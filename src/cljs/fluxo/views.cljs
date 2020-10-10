(ns fluxo.views
  (:require [re-frame.core :refer [subscribe]]
            [fluxo.onboarding :refer [onboarding-panel]]
            [fluxo.views.stream :refer [stream]]
            [fluxo.create-stream :refer [recipient-step amount-step
                                         duration-step confirmation-step]]))

(defmulti panel identity)

(defmethod panel :home [] [onboarding-panel])
(defmethod panel :create-stream/recipient [] [recipient-step])
(defmethod panel :create-stream/amount [] [amount-step])
(defmethod panel :create-stream/duration [] [duration-step])
(defmethod panel :create-stream/confirmation [] [confirmation-step])
(defmethod panel :stream/details [] [stream])

(defn main-panel []
  (let [db           (subscribe [:db/state])
        active-route (subscribe [:routes/active])]
    (fn []
      ;; TODO: Remove the debugging log or find a way to use environment
      ;; variables to instrument it only in development.
      (js/console.log "db state:" @db)
      [panel @active-route])))
