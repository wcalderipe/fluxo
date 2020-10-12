(ns fluxo.models.amount-step
  (:require-macros [fluxo.resources :refer [inline]])
  (:require [fluxo.models.create-stream :as create-stream]
            [fluxo.wallet :refer [mask-address]]
            [fluxo.money :refer [from-wei to-wei]]
            [re-frame.core :as rf]))

(defonce erc20-abi (.parse js/JSON (inline "erc20-abi.json")))

(defn- make-token [form-state]
  (merge (:token form-state) {:contract-abi erc20-abi}))

(rf/reg-event-fx
 ::on-submit
 (fn [_ [_ form-state]]
   {:fx [[:dispatch [::create-stream/add-amount (to-wei (:amount form-state))]]
         [:dispatch [::create-stream/add-token (make-token form-state)]]
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
