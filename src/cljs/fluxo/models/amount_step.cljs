(ns fluxo.models.amount-step
  (:require [fluxo.models.create-stream :as create-stream]
            [fluxo.wallet :refer [mask-address]]
            [fluxo.money :refer [from-wei to-wei]]
            [re-frame.core :as rf]))

(rf/reg-event-fx
 ::on-submit
 (fn [_ [_ form-state]]
   {:fx [[:dispatch [::create-stream/add-amount (to-wei (:amount form-state))]]
         [:dispatch [::create-stream/add-token (:token form-state)]]
         [:dispatch [:routes/redirect-to :create-stream/duration]]]}))

(rf/reg-sub
 ::amount-step
 :<- [:wallet/assets]
 :<- [::create-stream/recipient-addr]
 :<- [::create-stream/amount]
 (fn [[assets recipient-addr amount]]
   {:assets         assets
    :recipient-addr (mask-address recipient-addr)
    :amount         (from-wei amount)}))
