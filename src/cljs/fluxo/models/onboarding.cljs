(ns fluxo.models.onboarding
  (:require [fluxo.wallet :refer [mask-address]]
            ;; Indirectly load the :web3/ethereum coeffect
            [fluxo.web3]
            [re-frame.core :as rf]))

(rf/reg-event-fx
 ::connect-wallet
 [(rf/inject-cofx :web3/ethereum)]
 (fn [cofx _]
   {:wallet/request {:method     "eth_requestAccounts"
                     :provider   (:web3/ethereum cofx)
                     :on-success [:wallet/accounts-received]}}))

(rf/reg-sub
 ::onboarding
 (fn [db]
   (let [address   (get-in db [:wallet :address])
         ethereum? (get-in db [:web3 :ethereum-present?] false)]
     {:wallet-connected? (boolean address)
      :wallet-addr       address
      :ethereum?         ethereum?})))
