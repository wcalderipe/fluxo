(ns fluxo.web3
  (:require-macros [fluxo.resources :refer [inline]])
  (:require ["web3" :as Web3]
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
  (new (.. web3 -eth -Contract) abi address))

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

(reg-fx
 :web3/request-approval
 (fn [{:keys [provider token spender-addr
              amount wallet-addr on-success on-failure]}]
   (let [web3       (make-web3 provider)
         _          (set-contract-provider! web3 provider)
         token-abi  (:contract-abi token)
         token-addr (:address token)
         token      (make-contract web3 token-abi token-addr)
         tx         (make-approve-tx token spender-addr amount)]
     (-> (.send tx #js{:from wallet-addr})
         (.then #(dispatch (conj on-success %)))
         (.catch #(dispatch (conj on-failure %)))))))

(comment
  ;; Request user's accounts
  (request-accounts (ethereum!) #(prn %))

  ;; Request approval for ERC20 token
  (def test-dai-ropsten {:address      "0x2d69ad895797c880abce92437788047ba0eb7ff6"
                         :contract-abi (.parse js/JSON (inline "ropsten_test-dai-contract-abi.json"))})
  (def sablier-ropsten {:address      "0xc04Ad234E01327b24a831e3718DBFcbE245904CC"
                        :contract-abi (.parse js/JSON (inline "ropsten_sablier-contract-abi.json"))})

  ;; NOTE: Change this def value to the same address of the connected wallet.
  (def wallet-addr "CHANGE_ME")

  ;; Connecting the dots...
  (let [provider      (given-provider)
        web3          (make-web3 provider)
        _             (set-contract-provider! web3 provider)
        token-abi     (:contract-abi test-dai-ropsten)
        token-address (:address test-dai-ropsten)
        token         (make-contract web3 token-abi token-address)
        spender       (:address sablier-ropsten)
        amount        (fluxo.money/to-wei "200")
        tx            (.approve (.. token -methods) spender amount)]
    (.then (.send tx #js{:from wallet-addr}) #(js/console.log %)))

  ;; re-frame way...
  (re-frame.core/reg-event-db
   ::on-approval-success
   (fn [db [_ result]]
     (prn result)
     (assoc db ::success result)))

  (re-frame.core/reg-event-db
   ::on-approval-failure
   (fn [db [_ result]]
     (prn result)
     (assoc db ::failure result)))

  (re-frame.core/reg-event-fx
   ::request-approval
   (fn [_ [_ params]]
     {:web3/request-approval params}))

  (dispatch [::request-approval {:provider     (given-provider)
                                 :token        test-dai-ropsten
                                 :spender-addr (:address sablier-ropsten)
                                 :wallet-addr  wallet-addr
                                 :amount       (fluxo.money/to-wei "200")
                                 :on-success   [::on-approval-success]
                                 :on-failure   [::on-approval-failure]}])

  )
