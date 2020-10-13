(ns fluxo.sablier
  "re-frame effects to interact with Sablier Protocol."
  (:require-macros [fluxo.resources :refer [inline]])
  (:require [fluxo.web3 :as web3]
            ["web3-utils" :as web3-utils]
            [re-frame.core :as rf]))

(defn- now! []
  (js/Math.round (/ (-> (js/Date.) .getTime) 1000)))

(defn- hours->secs [h]
  (* h 3600))

(defn- make-create-stream-tx [^js/web3.eth.Contract sablier recipient-addr
                              amount token-addr start-time stop-time]
  (.createStream (.. sablier -methods) recipient-addr amount token-addr start-time stop-time))

(defn- to-bn [n]
  (.toBN web3-utils (if (= js/Number (type n)) (str n) n)))

(defn- calculate-streamble-amount
  "Returns the closest ether amount which is multiple of the difference between stop
  time and start time.

  See https://docs.sablier.finance/streams#the-deposit-gotcha"
  [start-time stop-time amount]
  (let [time-delta       (to-bn (- stop-time start-time))
        converted-amount (to-bn amount)
        remainder        (.mod converted-amount time-delta)
        result           (.sub converted-amount remainder)]
    (.toString result)))

(defonce sablier-ropsten {:address      "0xc04Ad234E01327b24a831e3718DBFcbE245904CC"
                          :contract-abi (.parse js/JSON (inline "ropsten_sablier-contract-abi.json"))})

(defn create-stream-fx [{:keys [provider wallet-addr token-addr recipient-addr
                                amount duration on-success on-failure]}]
  (let [web3             (web3/make-web3 provider)
        _                (web3/set-contract-provider! web3 provider)
        sablier-abi      (:contract-abi sablier-ropsten)
        sablier-addr     (:address sablier-ropsten)
        sablier          (web3/make-contract web3 sablier-abi sablier-addr)
        start-time       (+ (now!) (hours->secs 1))
        stop-time        (+ start-time (hours->secs duration))
        streamble-amount (calculate-streamble-amount start-time stop-time amount)
        tx               (make-create-stream-tx sablier recipient-addr streamble-amount token-addr start-time stop-time)]
    (let [p (.send tx #js{:from wallet-addr})]
      (.then p #(rf/dispatch (conj on-success %)))
      (when on-failure
        (.catch p #(rf/dispatch (conj on-failure %)))))))

(rf/reg-fx ::create-stream create-stream-fx)

(defn make-get-stream-tx [^js sablier stream-id]
  (.getStream (.. sablier -methods) stream-id))

(defn get-stream-fx [{:keys [provider wallet-addr stream-id
                             on-success on-failure]}]
  (let [web3         (web3/make-web3 provider)
        _            (web3/set-contract-provider! web3 provider)
        sablier-abi  (:contract-abi sablier-ropsten)
        sablier-addr (:address sablier-ropsten)
        sablier      (web3/make-contract web3 sablier-abi sablier-addr)
        tx           (make-get-stream-tx sablier stream-id)]
    (let [p (.call tx #js{:from wallet-addr})]
      (.then p #(rf/dispatch (conj on-success %)))
      (when on-failure
        (.catch p #(rf/dispatch (conj on-failure %)))))))

(rf/reg-fx ::get-stream get-stream-fx)

(comment
  ;; NOTE: Change this def value to the same address of the connected wallet.
  (def wallet-addr "CHANGE_ME")

  ;; Create Sablier stream ----------------------------------------------------
  (def recipient-addr "CHANGE_ME")

  ;; re-frame way...
  (re-frame.core/reg-event-fx
   ::on-create-stream
   (fn [_ [_ response]]
     (js/console.log response)))

  (create-stream-fx {:provider (given-provider)
                     :token-addr (:address test-dai-ropsten)
                     :wallet-addr wallet-addr
                     :recipient-addr recipient-addr
                     :amount (fluxo.money/to-wei "200")
                     :duration 2
                     :on-success [::on-create-stream]
                     :on-failure [::on-create-stream]})

  ;; Get Sablier stream ----------------------------------------------------
  (re-frame.core/reg-event-fx
   ::on-get-stream
   (fn [_ [_ response]]
     (js/console.log response)))

  (get-stream-fx {:provider (given-provider)
                  :wallet-addr wallet-addr
                  :stream-id "CHANGE_ME"
                  :on-success [::on-get-stream]
                  :on-failure [::on-get-stream]}))
