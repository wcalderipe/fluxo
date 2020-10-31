(ns fluxo.sablier
  "re-frame effects to interact with Sablier Protocol."
  (:require-macros [fluxo.resources :refer [inline]])
  (:require ["web3-utils" :as web3-utils]
            [clojure.spec.alpha :as s]
            [fluxo.config :refer [sablier-time-buffer]]
            [fluxo.web3 :as web3]
            [re-frame.core :as rf]
            [re-graph.core :as re-graph]))

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
  (let [web3              (web3/make-web3 provider)
        _                 (web3/set-contract-provider! web3 provider)
        sablier-abi       (:contract-abi sablier-ropsten)
        sablier-addr      (:address sablier-ropsten)
        sablier           (web3/make-contract web3 sablier-abi sablier-addr)
        start-time        (+ (now!) sablier-time-buffer)
        stop-time         (+ start-time (hours->secs duration))
        streamable-amount (calculate-streamble-amount start-time stop-time amount)
        tx                (make-create-stream-tx sablier
                                                 recipient-addr
                                                 streamable-amount
                                                 token-addr
                                                 start-time
                                                 stop-time)]
    (let [p (.send tx #js{:from wallet-addr})]
      (.then p #(rf/dispatch (conj on-success %)))
      (when on-failure
        (.catch p #(rf/dispatch (conj on-failure %)))))))

(rf/reg-fx ::create-stream create-stream-fx)

(defn- make-get-stream-tx [^js sablier stream-id]
  (.getStream (.. sablier -methods) stream-id))

(def ^:const gql-get-stream
  "subscription stream($id: String!) {
     stream(id: $id) {
      ratePerSecond
    }
  }")

(rf/reg-event-fx
 ::get-stream
 (fn [_ [_ {:keys [stream-id on-success]}]]
   {:fx [[:dispatch [::re-graph/subscribe ::get-stream
                     gql-get-stream {:id stream-id} on-success]]]}))

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

  (create-stream-fx {:provider       (given-provider)
                     :token-addr     (:address test-dai-ropsten)
                     :wallet-addr    wallet-addr
                     :recipient-addr recipient-addr
                     :amount         (fluxo.util/to-wei "200")
                     :duration       2
                     :on-success     [::on-create-stream]
                     :on-failure     [::on-create-stream]})

  ;; Get Sablier stream ----------------------------------------------------
  (re-frame.core/reg-event-fx
   ::on-get-stream
   (fn [_ [_ response]]
     (def r* response)
     (js/console.log response)))

  ;; See also the GraphQL playground at
  ;; https://thegraph.com/explorer/subgraph/sablierhq/sablier-ropsten
  (rf/dispatch [::get-stream {:stream-id "CHANGE_ME"
                              :on-success [::on-get-stream]}])
  )
