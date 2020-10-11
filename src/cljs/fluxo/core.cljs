(ns fluxo.core
  (:require [day8.re-frame.http-fx]
            [fluxo.config :as config]
            [fluxo.db :as db]
            [fluxo.routes :as routes]
            [fluxo.views :as views]
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
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (routes/start!)
  (rf/dispatch-sync [:db/initialize])
  (rf/dispatch-sync [::wallet/get-accounts])
  (dev-setup)
  (mount-root))
