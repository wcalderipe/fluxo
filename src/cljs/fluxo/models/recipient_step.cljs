(ns fluxo.models.recipient-step
  (:require [fluxo.models.create-stream :as create-stream]
            [re-frame.core :as rf]))

(rf/reg-event-fx
 ::on-submit
 (fn [_ [_ form-state]]
   {:fx [[:dispatch [::create-stream/add-recipient (:address form-state)]]
         ;; TODO: use an effect to redirect the user instead of an event.
         [:dispatch [:routes/redirect-to :create-stream/amount]]]}))

(rf/reg-sub
 ::recipient-step
 (fn []
   [(rf/subscribe [::create-stream/recipient-addr])])
 (fn [[recipient-addr]]
   {:recipient-addr recipient-addr}))
