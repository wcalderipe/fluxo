(ns fluxo.views.amount-step-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljsjs.react]
            [day8.re-frame.test :refer [run-test-sync]]
            [devcards.core :as dc :refer-macros [defcard]]
            [re-frame.core :as rf]
            [fluxo.models.create-stream :as create-stream]
            [fluxo.test-helper :refer [fixture-re-frame with-mounted-component found-in]]
            [fluxo.views.amount-step :as view]))

(use-fixtures :each (fixture-re-frame))

(defcard
  (dc/reagent
   [:div
    [:h3 [:i "Initial state"]]
    [view/amount-step-component {:assets         [{:name "Foo" :symbol "FOO"}
                                                  {:name "Bar" :symbol "BAR"}]
                                 :amount         "200"
                                 :recipient-addr "0xfoo111bar"}]]))

(deftest amount-step-view-test
  (run-test-sync
   (rf/dispatch [:db/initialize])
   (rf/dispatch [::create-stream/add-recipient "0xfoo111bar"])

   (testing "renders recipient address"
     (with-mounted-component [view/amount-step]
       (fn [_ div]
         (is (found-in #"How much do you want to sent to 0xfoo...bar" div)))))))
