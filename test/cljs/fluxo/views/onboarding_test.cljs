(ns fluxo.views.onboarding-test
  (:require [cljs.test :refer-macros [testing is use-fixtures]]
            [cljsjs.react]
            [day8.re-frame.test :refer-macros [run-test-sync]]
            [devcards.core :as dc :refer-macros [deftest defcard]]
            [re-frame.core :as rf]
            [fluxo.test-helper :refer [fixture-re-frame with-mounted-component found-in]]
            [fluxo.wallet :as wallet]
            [fluxo.views.onboarding :as onboarding]))

(use-fixtures :each (fixture-re-frame))

(defcard
  (dc/reagent
   [:div
    [:h2 [:i "Initial state"]]
    [onboarding/onboarding-component {:wallet-connected? false}]

    [:h2 [:i "Wallet connected"]]
    [onboarding/onboarding-component {:wallet-connected? true
                                      :wallet-addr       "0xfoo111bar"}]]))
(deftest onboarding-step-test
  (run-test-sync
   (rf/reg-cofx
    :web3/ethereum
    (fn [cofx]
      (assoc cofx :web3/ethereum :fake-ethereum)))

   (rf/dispatch [:db/initialize])

   (with-mounted-component [onboarding/onboarding]
     (fn [_ div]
       (is (found-in #"It seems you haven't connected your wallet" div))))

   (rf/dispatch [:wallet/accounts-received ["0x1foo222bar"]])

   (with-mounted-component [onboarding/onboarding]
     (fn [_ div]
       (is (found-in #"Your connected wallet is 0x1fo...bar" div))))))
