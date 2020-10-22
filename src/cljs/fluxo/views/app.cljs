(ns fluxo.views.app
  (:require [fluxo.views.add-token :refer [add-token]]
            [fluxo.views.amount-step :refer [amount-step]]
            [fluxo.views.confirmation-step :refer [confirmation-step]]
            [fluxo.views.duration-step :refer [duration-step]]
            [fluxo.views.onboarding :refer [onboarding]]
            [fluxo.views.recipient-step :refer [recipient-step]]
            [fluxo.views.stream :refer [stream]]
            [re-frame.core :refer [subscribe]]))

(defmulti panel identity)

(defmethod panel :home [] [onboarding])
(defmethod panel :create-stream/recipient [] [recipient-step])
(defmethod panel :create-stream/amount [] [amount-step])
(defmethod panel :create-stream/duration [] [duration-step])
(defmethod panel :create-stream/confirmation [] [confirmation-step])
(defmethod panel :token/new [] [add-token])
(defmethod panel :stream/details [] [stream])

(defn main-panel []
  (let [db           (subscribe [:db/state])
        active-route (subscribe [:routes/active])]
    (fn []
      ;; TODO: Remove the debugging log or find a way to use environment
      ;; variables to instrument it only in development.
      (js/console.log "db state:" @db)
      [panel @active-route])))
