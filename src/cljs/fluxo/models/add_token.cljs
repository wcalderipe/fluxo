(ns fluxo.models.add-token
  (:require [fluxo.token :as token]
            [fluxo.wallet :as wallet]
            [fluxo.web3 :as web3]
            [re-frame.core :as rf]))

(rf/reg-event-fx
 ::on-submit
 (fn [cofx [_ form-state]]
   {:fx [[:dispatch [:wallet/add-asset form-state]]
         [:dispatch [:routes/redirect-to :create-stream/amount]]]}))
