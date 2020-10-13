(ns fluxo.views.onboarding
  (:require [re-frame.core :as rf]
            [fluxo.models.onboarding :as model]
            [fluxo.wallet :as wallet]
            [fluxo.routes :refer [url-for]]))

(defn- connect-your-wallet [{on-click :on-click}]
  [:div.cta
   [:p "It seems you haven't connected your wallet yet. Let's do that first!"]
   [:button {:on-click on-click}
    "Connect to MetaMask"]])

(defn- start-streaming-money [wallet-addr]
  [:div.cta
   [:p "Your connected wallet is " wallet-addr]
   [:a.button {:href (url-for :create-stream/recipient)} "Create your first stream!"]])

(defn- install-metamask []
  [:div.cta
   [:p "You need to download & install MetaMask to use Fluxo."]
   [:a.button {:href   "https://metamask.io/download.html"
               :target "_blank"} "Click here to download it"]])

(defn onboarding-component [{:keys [wallet-addr wallet-connected? ethereum?]}]
  [:div
   [:p "Fluxo is a decentralized-app to stream money in the Ethereum blockchain."]
   (if ethereum?
     (if wallet-connected?
       [start-streaming-money wallet-addr]
       [connect-your-wallet {:on-click #(rf/dispatch [::model/connect-wallet])}])
     [install-metamask])])

(defn onboarding []
  (let [model (rf/subscribe [::model/onboarding])]
    (fn []
      (when (:ethereum? @model)
        (rf/dispatch [::wallet/get-accounts]))
      [:section#onboarding
       [:h1 "Welcome to Fluxo"]
       [onboarding-component @model]])))
