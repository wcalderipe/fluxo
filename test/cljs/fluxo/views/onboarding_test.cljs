(ns fluxo.views.onboarding-test
  (:require [cljs.test :refer-macros [testing is use-fixtures]]
            [cljsjs.react]
            [day8.re-frame.test :refer-macros [run-test-sync]]
            [devcards.core :as dc :refer-macros [deftest defcard]]
            [fluxo.views.onboarding :as onboarding]))

(defcard
  (dc/reagent
   [:div
    [:h2 [:i "Initial state"]]
    [onboarding/onboarding-component {:wallet-connected? false}]

    [:h2 [:i "Wallet connected"]]
    [onboarding/onboarding-component {:wallet-connected? true
                                      :wallet-addr       "0xfoo111bar"}]]))
