(ns fluxo.onboarding
  (:require [fluxo.wallet :refer [mask-address]]
            [re-frame.core :refer [subscribe]]))

(defn connect-your-wallet [{on-click :on-click}]
  [:div
   [:p "It seems you haven't connected your wallet yet. Let's do that first!"]
   [:button {:on-click on-click}
    "Connect to MetaMask"]])

(defn start-streaming-money [{wallet-address :wallet-address
                              on-click :on-click}]
  [:div
   [:p "Your connected wallet is " (mask-address wallet-address)]
   [:button {:on-click on-click} "Create your first stream!"]])

(defn onboarding-panel []
  (let [wallet-connected? @(subscribe [:wallet/connected?])
        wallet-address @(subscribe [:wallet/address])]
    (fn []
      [:div
       [:h1 "Welcome to Fluxo"]
       [:p "Fluxo is a decentralized-app to stream money in the Ethereum blockchain."]
       (if wallet-connected?
         [start-streaming-money {:wallet-address wallet-address
                                 :on-click #(js/console.log "stream..")}]
         [connect-your-wallet {:on-click #(js/console.log "connect to wallet")}])])))
