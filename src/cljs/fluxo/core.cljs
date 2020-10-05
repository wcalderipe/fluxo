(ns fluxo.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as re-frame]
            [day8.re-frame.http-fx]
            [fluxo.etherscan]
            [fluxo.views :as views]
            [fluxo.db :as db]
            [fluxo.config :as config]
            [fluxo.routes :as routes]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [:db/initialize])
  (routes/app-routes)
  (dev-setup)
  (mount-root))
