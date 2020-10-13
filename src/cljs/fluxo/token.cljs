(ns fluxo.token
  "re-frame effects to interact with ERC20 tokens."
  (:require-macros [fluxo.resources :refer [inline]])
  (:require [fluxo.web3 :as web3]
            [re-frame.core :as rf]))

(defonce erc20-abi (.parse js/JSON (inline "erc20-abi.json")))

(defn- make-approve-tx [^js token spender amount]
  (.approve (.. token -methods) spender amount))

(defn approve-fx
  "Sends an approval request for an ERC20 token."
  [{:keys [provider token-addr spender-addr amount
           wallet-addr on-success on-failure]}]
  (let [web3  (web3/make-web3 provider)
        _     (web3/set-contract-provider! web3 provider)
        token (web3/make-contract web3 erc20-abi token-addr)
        tx    (make-approve-tx token spender-addr amount)]
    (let [p (.send tx #js{:from wallet-addr})]
      (.then p #(rf/dispatch (conj on-success %)))
      (when on-failure
        (.catch p #(rf/dispatch (conj on-failure %)))))))

(rf/reg-fx ::approve approve-fx)

(defn- make-balance-of-tx [^js token addr]
  (.balanceOf (.. token -methods) addr))

(defn balance-of-fx
  "Gets the balance of ERC20 token for the given address. If the address is nil,
  for the given wallet address."
  [{:keys [provider wallet-addr addr
           token-addr on-success on-failure]}]
  (let [web3  (web3/make-web3 provider)
        _     (web3/set-contract-provider! web3 provider)
        token (web3/make-contract web3 erc20-abi token-addr)
        tx    (make-balance-of-tx token (or addr wallet-addr))]
    (let [p (.call tx #js{:from wallet-addr})]
      (.then p #(rf/dispatch (conj on-success %)))
      (when on-failure
        (.catch p #(rf/dispatch (conj on-failure %)))))))

(rf/reg-fx ::balance-of balance-of-fx)

(comment
  ;; NOTE: Change this def value to the same address of the connected wallet.
  (def wallet-addr "CHANGE_ME")

  ;; Request approval for ERC20 token -----------------------------------------
  (rf/reg-event-fx
   ::on-approve-success
   (fn [_ [_ resp]]
     (js/console.log (fluxo.money/from-wei resp))
     {}))

  (approve-fx {:provider     (web3/given-provider)
               :wallet-addr  wallet-addr
               :token-addr   "0x2d69ad895797c880abce92437788047ba0eb7ff6"
               :spender-addr "0xc04Ad234E01327b24a831e3718DBFcbE245904CC" ;; Sablier (Ropsten)
               :amount       (fluxo.money/to-wei "200")
               :on-success   [::on-approve-success]})

  ;; Get ERC20 token balance --------------------------------------------------
  (rf/reg-event-fx
   ::on-balance-success
   (fn [_ [_ resp]]
     (js/console.log (fluxo.money/from-wei resp))
     {}))

  (balance-of-fx {:provider    (web3/given-provider)
                  :wallet-addr wallet-addr
                  :token-addr  "0x2d69ad895797c880abce92437788047ba0eb7ff6" ;; TestDAI (Ropsten)
                  :on-success  [::on-balance-success]})

  )
