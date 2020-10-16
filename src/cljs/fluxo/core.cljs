(ns fluxo.core
  (:require [day8.re-frame.http-fx]
            [fluxo.config :as config]
            [fluxo.db :as db]
            [fluxo.routes :as routes]
            [fluxo.views.app :as app]
            [fluxo.web3 :as web3]
            [fluxo.models.stream :as stream]
            [fluxo.wallet :as wallet]
            [re-frame.core :as rf]
            [reagent.dom :as rdom]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [app/main-panel] root-el)))

(defn init []
  (routes/start!)
  (rf/dispatch-sync [:db/initialize])
  (rf/dispatch [::web3/save-ethereum-presence])
  (rf/dispatch [::stream/fetch-stream])
  (wallet/listen-accounts-change (web3/ethereum!))
  (dev-setup)
  (mount-root))
