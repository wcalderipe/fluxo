(ns fluxo.onboarding
  (:require [fluxo.wallet :refer [mask-address]]
            [fluxo.routes :refer [url-for]]
            [re-frame.core :refer [dispatch subscribe reg-event-fx inject-cofx]]))

(defn fetch-accounts-handler [cofx _]
  {:wallet/request {:method     "eth_accounts"
                    :provider   (:web3/ethereum cofx)
                    :on-success [:wallet/accounts-received]}})

(reg-event-fx
 :onboarding/fetch-accounts
 [(inject-cofx :web3/ethereum)]
 fetch-accounts-handler)

(defn connect-wallet-handler [cofx _]
  {:wallet/request {:method     "eth_requestAccounts"
                    :provider   (:web3/ethereum cofx)
                    :on-success [:wallet/accounts-received]}})

(reg-event-fx
 :onboarding/connect-wallet
 [(inject-cofx :web3/ethereum)]
 connect-wallet-handler)

(defn connect-your-wallet [{on-click :on-click}]
  [:div
   [:p "It seems you haven't connected your wallet yet. Let's do that first!"]
   [:button {:on-click on-click}
    "Connect to MetaMask"]])

(defn start-streaming-money [{wallet-address :wallet-address}]
  [:div
   [:p "Your connected wallet is " (mask-address wallet-address)]
   [:a {:href (url-for :create-stream/recipient)} "Create your first stream!"]])

(defn onboarding-component [model]
  [:div
   [:h1 "Welcome to Fluxo"]
   [:p "Fluxo is a decentralized-app to stream money in the Ethereum blockchain."]
   (if (:wallet-connected? model)
     [start-streaming-money {:wallet-address (:wallet-addr model)}]
     [connect-your-wallet {:on-click #(dispatch [:onboarding/connect-wallet])}])])

(defn onboarding-panel []
  (let [_                 (dispatch [:onboarding/fetch-accounts])
        wallet-connected? (subscribe [:wallet/connected?])
        wallet-addr       (subscribe [:wallet/address])]
    (fn []
      [onboarding-component {:wallet-connected? @wallet-connected?
                             :wallet-addr       @wallet-addr}])))
