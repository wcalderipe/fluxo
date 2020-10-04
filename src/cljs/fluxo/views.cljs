(ns fluxo.views
  (:require [re-frame.core :refer [subscribe]]
            [fluxo.onboarding :refer [onboarding-panel]]
            [fluxo.create-stream :refer [recipient-step]]))

(defmulti panel identity)

(defmethod panel :home [] [onboarding-panel])
(defmethod panel :create-stream/recipient [] [recipient-step])

(defn main-panel []
  (let [db (subscribe [:db/state])
        active-route (subscribe [:routes/active])]
    (fn []
      ;; TODO: Remove the debugging log or find a way to use environment
      ;; variables to instrument it only in development.
      (js/console.log "db state:" @db)
      [:div
       [panel @active-route]])))
