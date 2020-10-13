(ns fluxo.views.amount-step-test
  (:require [cljsjs.react]
            [day8.re-frame.test :refer [run-test-sync]]
            [devcards.core :as dc :refer-macros [defcard]]
            [fluxo.views.amount-step :as view]))

(defcard
  (dc/reagent
   [:div
    [:h3 [:i "Initial state"]]
    [view/amount-step-component {:assets         [{:name "Foo" :symbol "FOO"}
                                                  {:name "Bar" :symbol "BAR"}]
                                 :amount         "200"
                                 :recipient-addr "0xfoo111bar"}]]))
