(ns fluxo.models.duration-step-test
  (:require [cljs.test :refer-macros [testing is use-fixtures]]
            [cljsjs.react]
            [day8.re-frame.test :refer [run-test-sync]]
            [devcards.core :as dc :refer-macros [deftest]]
            [fluxo.models.duration-step :as model]
            [fluxo.models.create-stream :as create-stream]
            [fluxo.test-helper :refer [fixture-re-frame]]
            [re-frame.core :as rf]))

(use-fixtures :each (fixture-re-frame))

(deftest initial-state-test
  (run-test-sync
   (rf/dispatch [::create-stream/add-recipient "0xfoo111bar"])
   (rf/dispatch [::create-stream/add-token {:symbol "BAR"}])
   (rf/dispatch [::create-stream/add-amount "200000000000000000000"])
   (rf/dispatch [::create-stream/add-duration 2.5])

   (let [duration-step (rf/subscribe [::model/duration-step])]
     (is (= {:recipient-addr "0xfoo111bar"
             :token-symbol   "BAR"
             :amount         "200"
             :duration       2.5} @duration-step)))))

(deftest on-submit-test
  (run-test-sync
   (rf/dispatch [:db/initialize])

   (let [duration-step (rf/subscribe [::model/duration-step])
         active-route (rf/subscribe [:routes/active])]

     (rf/dispatch [::model/on-submit {:duration "2.5"}])

     (testing "converts duration from string to number"
       (is (= 2.5 (:duration @duration-step))))

     (testing "redirects to the confirmation step"
       (is (= :create-stream/confirmation @active-route))))))
