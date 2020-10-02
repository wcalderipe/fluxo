(ns fluxo.views
  (:require [re-frame.core :refer [subscribe]]
            [fluxo.onboarding :refer [onboarding-panel]]))

(defmulti panel identity)

(defmethod panel :home-panel [] [onboarding-panel])

(defn main-panel []
  (let [db (subscribe [:db/state])
        active-panel (subscribe [:routes/active-panel])]
    (fn []
      ;; TODO: Remove the debugging log or find a way to use environment
      ;; variables to instrument it only in development.
      (js/console.log "db state:" @db)
      [:div
       [panel @active-panel]])))
