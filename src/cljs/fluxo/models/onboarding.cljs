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
 (fn []
   [(rf/subscribe [:wallet/address])])
 (fn [[address]]
   {:wallet-connected? (boolean address)
    :wallet-addr       (mask-address address)}))
