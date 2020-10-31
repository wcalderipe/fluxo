(ns fluxo.models.amount-step-test
  (:require [cljs.test :refer-macros [async testing is use-fixtures]]
            [cljsjs.react]
            [day8.re-frame.test :refer [run-test-sync]]
            [devcards.core :as dc :refer-macros [deftest]]
            [fluxo.models.amount-step :as model]
            [fluxo.models.create-stream :as create-stream]
            [fluxo.test-helper :refer [fixture-re-frame]]
            [fluxo.util :refer [from-wei to-wei]]
            [re-frame.core :as rf]))

(use-fixtures :each (fixture-re-frame))

(deftest initial-state-test
  (run-test-sync
   (rf/dispatch [:db/initialize])
   (rf/dispatch [::create-stream/add-recipient "0xfoo111bar"])

   (let [amount-step (rf/subscribe [::model/amount-step])]
     (is (= "0xfoo111bar" (:recipient-addr @amount-step)))
     (is (= nil (:amount @amount-step))))))

(deftest on-submit-test
  (run-test-sync
   (let [recipient-step (rf/subscribe [::model/amount-step])
         amount         (rf/subscribe [::create-stream/amount])
         token          (rf/subscribe [::create-stream/token])
         active-route   (rf/subscribe [:routes/active])]

     (rf/dispatch [::model/on-submit {:amount "200"
                                      :token {:symbol "BAR"
                                              :address "0xBAR"}}])
     (testing "converts amount to ether"
       (is (= "200000000000000000000" @amount)))

     (testing "redirects to the amount step"
       (is (= :create-stream/duration @active-route))))))
