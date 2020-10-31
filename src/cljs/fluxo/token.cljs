(ns fluxo.token
  "re-frame effects to interact with ERC20 tokens."
  (:require-macros [fluxo.resources :refer [inline]])
  (:require [clojure.spec.alpha :as s]
            [fluxo.web3 :as web3]
            [re-frame.core :as rf]))

(defonce erc20-abi (.parse js/JSON (inline "erc20-abi.json")))

(defn- make-tx
  "Creates a tx object by invoking dynamically an ERC20 method and its arguments.

  Examples:
  (make-tx token :name)
  (make-tx token :approve spender-addr ether-amount)"
  [^js token method & args]
  (apply js-invoke (.. token -methods) (name method) args))

(defn- make-token! [provider token-addr]
  (let [web3           (web3/make-web3 provider)
        _              (web3/set-contract-provider! web3 provider)]
    (web3/make-contract web3 erc20-abi token-addr)))

(s/def ::provider some?)
(s/def ::token-addr ::web3/address)
(s/def ::wallet-addr ::web3/address)
(s/def ::method (s/or :with-args    (s/cat :invoker keyword? :method keyword? :args vector?)
                      :without-args (s/cat :invoker keyword? :method keyword?)))
(s/def ::on-success vector?)
(s/def ::on-failure vector?)

(s/fdef tx!
  :args (s/keys :req-un [::provider ::token-addr ::wallet-addr ::method ::on-success]
                :opt-un [::on-failure])
  :ret js/Promise)

(defn- tx!
  "Invokes an ERC20 method in the given provider.

  Providers use different methods depending on the contract function
  signature. For read functions uses `.call' and for write uses `.send'.

  Getting a token name:

  (tx! {:provider    (web3/given-provider)
        :token-addr  token-addr
        :wallet-addr wallet-addr
        :method      [:call :name]   ; provider method & fn name
        :on-success  [:on-success]})

  Approve spend amount:

  (tx! {:provider    (web3/given-provider)
        :token-addr  token-addr
        :wallet-addr wallet-addr
        :method      [:send :approve [spender-addr amount]]  ; provider method, fn name & args
        :on-success  [:on-success]})"
  [{:keys [provider token-addr wallet-addr method on-success on-failure]}]
  (let [token          (make-token! provider token-addr)
        m              (second (s/conform ::method method))
        method-name    (:method m)
        method-args    (:args m)
        method-invoker (name (:invoker m))
        tx             (apply make-tx token method-name method-args)
        promise        (js-invoke tx method-invoker #js{:from wallet-addr})]
    (.then promise #(rf/dispatch (conj on-success %)))
    (when on-failure
      (.catch promise #(rf/dispatch (conj on-failure %))))))

(defn approve-fx
  "Sends an approval request for an ERC20 token."
  [{:keys [provider token-addr spender-addr amount
           wallet-addr on-success on-failure]}]
  (tx! {:provider    provider
        :token-addr  token-addr
        :wallet-addr wallet-addr
        :method      [:send :approve [spender-addr amount]]
        :on-success  on-success
        :on-failure  on-failure}))

(rf/reg-fx ::approve approve-fx)

(defn balance-of-fx
  "Gets the balance of ERC20 token for the given address. If the address is nil,
  for the given wallet address."
  [{:keys [provider wallet-addr addr
           token-addr on-success on-failure]}]
  (tx! {:provider    provider
        :token-addr  token-addr
        :wallet-addr wallet-addr
        :method      [:call :balanceOf [(or addr wallet-addr)]]
        :on-success  on-success
        :on-failure  on-failure}))

(rf/reg-fx ::balance-of balance-of-fx)

(defn name-fx
  [{:keys [provider wallet-addr token-addr on-success on-failure]}]
  (tx! {:provider    provider
        :token-addr  token-addr
        :wallet-addr wallet-addr
        :method      [:call :name]
        :on-success  on-success
        :on-failure  on-failure}))

(rf/reg-fx ::name name-fx)

(comment
  ;; NOTE: Change this def value to the same address of the connected wallet.
  (def wallet-addr "CHANGE_ME")

  ;; Request approve ----------------------------------------------------------
  (rf/reg-event-fx
   ::on-approve-success
   (fn [_ [_ resp]]
     (js/console.log (fluxo.util/from-wei resp))
     {}))

  (approve-fx {:provider     (web3/given-provider)
               :wallet-addr  wallet-addr
               :token-addr   "0x2d69ad895797c880abce92437788047ba0eb7ff6"
               :spender-addr "0xc04Ad234E01327b24a831e3718DBFcbE245904CC" ;; Sablier (Ropsten)
               :amount       (fluxo.util/to-wei "200")
               :on-success   [::on-approve-success]})

  ;; Get balance --------------------------------------------------------------
  (rf/reg-event-fx
   ::on-balance-success
   (fn [_ [_ resp]]
     (js/console.log (fluxo.util/from-wei resp))
     {}))

  (balance-of-fx {:provider    (web3/given-provider)
                  :wallet-addr wallet-addr
                  :token-addr  "0x2d69ad895797c880abce92437788047ba0eb7ff6" ;; TestDAI (Ropsten)
                  :on-success  [::on-balance-success]})

  ;; Get name -----------------------------------------------------------------
  (rf/reg-event-fx
   ::on-success
   (fn [_ [_ resp]]
     (js/console.log resp)
     {}))

  (name-fx {:provider    (web3/given-provider)
            :wallet-addr wallet-addr
            :token-addr  "0x2d69ad895797c880abce92437788047ba0eb7ff6" ;; TestDAI (Ropsten)
            :on-success  [::on-success]})

  )
