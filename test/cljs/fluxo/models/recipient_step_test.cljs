(ns fluxo.models.recipient-step-test
  (:require [cljs.test :refer-macros [testing is use-fixtures]]
            [cljsjs.react]
            [day8.re-frame.test :refer [run-test-sync]]
            [devcards.core :as dc :refer-macros [deftest]]
            [fluxo.models.recipient-step :as model]
            [fluxo.test-helper :refer [fixture-re-frame]]
            [re-frame.core :as rf]))

(use-fixtures :each (fixture-re-frame))

(deftest initial-state-test
  (run-test-sync
   (let [recipient-step (rf/subscribe [::model/recipient-step])]
     (is (= {:recipient-addr nil} @recipient-step)))))

(deftest on-submit-test
  (run-test-sync
   (let [recipient-step (rf/subscribe [::model/recipient-step])
         active-route (rf/subscribe [:routes/active])]

     (rf/dispatch [::model/on-submit {:address "0xfoo111bar"}])

     (is (= {:recipient-addr "0xfoo111bar"}))

     (testing "redirects to the amount step"
       (is (= :create-stream/amount @active-route))))))
