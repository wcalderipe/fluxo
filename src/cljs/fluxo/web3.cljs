(ns fluxo.web3
  (:require-macros [fluxo.resources :refer [inline]])
  (:require ["web3" :as Web3]
            ["web3-utils" :as web3-utils]
            [re-frame.core :refer [dispatch reg-cofx reg-fx]]))

(defn ethereum!
  "Get the ethereum propery from js/window."
  []
  (.-ethereum js/window))

(defn given-provider []
  (.. Web3 -givenProvider))

(defn make-web3 [provider]
  (Web3. provider))

(defn contract-class [web3]
  (.. web3 -eth -Contract))

(defn make-contract [web3 abi address]
  (new (.. web3 -eth -Contract) (clj->js abi) address))

(defn set-contract-provider! [web3 provider]
  (.setProvider (contract-class web3) provider))

(defn request-accounts
  "Send a request to get the user's accounts using the ethereum object."
  [ethereum on-success]
  (-> (.request ethereum (clj->js {:method "eth_requestAccounts"}))
      (.then #(on-success (js->clj %)))))

(defn fetch-accounts
  [ethereum on-success]
  (-> (.request ethereum (clj->js {:method "eth_accounts"}))
      (.then #(on-success (js->clj %)))))

(reg-cofx
 :web3/ethereum
 (fn [cofx]
   (assoc cofx :web3/ethereum (ethereum!))))

(reg-cofx
 :web3/provider
 (fn [cofx]
   (assoc cofx :web3/provider (given-provider))))

(defn- make-approve-tx [^js/web3.eth.Contract token spender amount]
  (.approve (.. token -methods) spender amount))

(defonce erc20-abi (.parse js/JSON (inline "erc20-abi.json")))

(defn request-approval-fx [{:keys [provider token-addr spender-addr amount
                                   wallet-addr on-success on-failure]}]
  (let [web3  (make-web3 provider)
        _     (set-contract-provider! web3 provider)
        token (make-contract web3 erc20-abi token-addr)
        tx    (make-approve-tx token spender-addr amount)]
    (-> (.send tx #js{:from wallet-addr})
        (.then #(dispatch (conj on-success %)))
        (.catch #(dispatch (conj on-failure %))))))

(reg-fx
 :web3/request-approval
 request-approval-fx)

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
  (let [web3             (make-web3 provider)
        _                (set-contract-provider! web3 provider)
        sablier-abi      (:contract-abi sablier-ropsten)
        sablier-addr     (:address sablier-ropsten)
        sablier          (make-contract web3 sablier-abi sablier-addr)
        start-time       (+ (now!) (hours->secs 1))
        stop-time        (+ start-time (hours->secs duration))
        streamble-amount (calculate-streamble-amount start-time stop-time amount)
        tx               (make-create-stream-tx sablier recipient-addr streamble-amount token-addr start-time stop-time)]
    (let [p (.send tx #js{:from wallet-addr})]
      (.then p #(dispatch (conj on-success %)))
      (when on-failure
        (.catch p #(dispatch (conj on-failure %)))))))

(reg-fx
 :web3/create-stream
 create-stream-fx)

(defn make-get-stream-tx [^js/web3.eth.Contract sablier stream-id]
  (.getStream (.. sablier -methods) stream-id))

(defn get-stream-fx [{:keys [provider wallet-addr stream-id
                             on-success on-failure]}]
  (let [web3         (make-web3 provider)
        _            (set-contract-provider! web3 provider)
        sablier-abi  (:contract-abi sablier-ropsten)
        sablier-addr (:address sablier-ropsten)
        sablier      (make-contract web3 sablier-abi sablier-addr)
        tx           (make-get-stream-tx sablier stream-id)]
    (let [p (.call tx #js{:from wallet-addr})]
      (.then p #(dispatch (conj on-success %)))
      (when on-failure
        (.catch p #(dispatch (conj on-failure %)))))))

(reg-fx
 :web3/get-stream
 get-stream-fx)

(comment
  (def test-dai-ropsten {:address      "0x2d69ad895797c880abce92437788047ba0eb7ff6"
                         :contract-abi (.parse js/JSON (inline "erc20-abi.json"))})
  (def sablier-ropsten {:address      "0xc04Ad234E01327b24a831e3718DBFcbE245904CC"
                        :contract-abi (.parse js/JSON (inline "ropsten_sablier-contract-abi.json"))})

  ;; NOTE: Change this def value to the same address of the connected wallet.
  (def wallet-addr "CHANGE_ME")

  ;; Request user's accounts --------------------------------------------------
  (request-accounts (ethereum!) #(prn %))

  ;; Sablier's deposit gotcha -------------------------------------------------
  ;; See https://docs.sablier.finance/streams#the-deposit-gotcha
  (let [delta     (to-bn "2592000")
        amount    (to-bn "3000000000000000000000")
        remainder (.mod amount delta)
        result    (.sub amount remainder)]
    (.toString result)) ;; Expected return: 2999999999999998944000

  ;; Request approval for ERC20 token -----------------------------------------
  (let [provider      (given-provider)
        web3          (make-web3 provider)
        _             (set-contract-provider! web3 provider)
        token-abi     (:contract-abi test-dai-ropsten)
        token-address (:address test-dai-ropsten)
        token         (make-contract web3 token-abi token-address)
        spender       (:address sablier-ropsten)
        ;; amount        (fluxo.money/to-wei "3000")
        amount         "2999999999999998944000"
        tx            (.approve (.. token -methods) spender amount)]
    (.then (.send tx #js{:from wallet-addr}) #(js/console.log %)))

  ;; re-frame way...
  (re-frame.core/reg-event-fx
   ::on-spend-approval
   (fn [_ [_ result]]
     (js/console.log result)))

  (request-approval-fx {:provider     (given-provider)
                        :token-addr   (:address test-dai-ropsten)
                        :token-abi    (:contract-abi test-dai-ropsten)
                        :spender-addr (:address sablier-ropsten)
                        :wallet-addr  wallet-addr
                        :amount       (fluxo.money/to-wei "200")
                        :on-success   [::on-spend-approval]
                        :on-failure   [::on-spend-approval]})

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
                  :on-failure [::on-get-stream]})

  )
