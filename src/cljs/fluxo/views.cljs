(ns fluxo.views
  (:require [re-frame.core :as re-frame]))

(defmulti panel identity)

(defn home-panel []
  [:div [:h1 "Home!"]])

(defmethod panel :home-panel [] [home-panel])

(defn main-panel []
  (let [active-panel @(re-frame/subscribe [:routes/active-panel])]
    (fn []
      [:div [panel active-panel]])))
