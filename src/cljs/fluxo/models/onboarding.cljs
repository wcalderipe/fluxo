(ns fluxo.models.onboarding
  (:require [re-frame.core :as rf]
            [fluxo.wallet :refer [mask-address]]))

(rf/reg-event-fx
 ::connect-wallet
 [(rf/inject-cofx :web3/ethereum)]
 (fn [cofx _]
   {:wallet/request {:method     "eth_requestAccounts"
                     :provider   (:web3/ethereum cofx)
                     :on-success [:wallet/accounts-received]}} ))

(rf/reg-sub
 ::onboarding
 (fn []
   [(rf/subscribe [:wallet/connected?])
    (rf/subscribe [:wallet/address])])
 (fn [[connected? address]]
   {:wallet-connected? connected?
    :wallet-addr       (mask-address address)}))
