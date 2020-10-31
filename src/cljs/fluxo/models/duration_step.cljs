(ns fluxo.models.duration-step
  (:require [fluxo.models.create-stream :as create-stream]
            [fluxo.util :refer [from-wei]]
            [fluxo.wallet :refer [mask-address]]
            [re-frame.core :as rf]))

(rf/reg-event-fx
 ::on-submit
 (fn [_ [_ form-state]]
   {:fx [[:dispatch [::create-stream/add-duration (js/parseFloat (:duration form-state))]]
         [:dispatch [:routes/redirect-to :create-stream/confirmation]]]}))

(rf/reg-sub
 ::duration-step
 :<- [::create-stream/recipient-addr]
 :<- [::create-stream/token]
 :<- [::create-stream/amount]
 :<- [::create-stream/duration]
 (fn [[recipient-addr token amount duration]]
   {:recipient-addr recipient-addr
    :token-symbol   (:symbol token)
    :amount         (from-wei amount)
    :duration       duration}))
