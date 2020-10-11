(ns fluxo.models.amount-step-test
  (:require [cljs.test :refer-macros [async testing is use-fixtures]]
            [cljsjs.react]
            [day8.re-frame.test :refer [run-test-sync]]
            [devcards.core :as dc :refer-macros [deftest]]
            [fluxo.models.amount-step :as model]
            [fluxo.models.create-stream :as create-stream]
            [fluxo.money :refer [from-wei to-wei]]
            [fluxo.etherscan :as etherscan]
            [fluxo.test-helper :refer [fixture-re-frame]]
            [re-frame.core :as rf]))

(use-fixtures :each (fixture-re-frame))

(defn- stub-etherscan [{expected-params :expected-params
                        response :response}]
  (rf/reg-event-fx
   ::etherscan/get-contract-abi
   (fn [_ [_ params]]
     (is (= expected-params params))

     {:dispatch (conj (:on-success params) response)})))

(deftest initial-state-test
  (run-test-sync
   (rf/dispatch [:db/initialize])
   (rf/dispatch [::create-stream/add-recipient "0xfoo111bar"])

   (let [amount-step (rf/subscribe [::model/amount-step])]
     (is (= "0xfoo...bar" (:recipient-addr @amount-step)))
     (is (= nil (:amount @amount-step))))))

(deftest on-submit-test
  (run-test-sync
   (stub-etherscan {:expected-params {:address    "0xBAR"
                                      :on-success [::model/on-token-contract-success]}
                    :response        :fake-abi})

   (let [recipient-step (rf/subscribe [::model/amount-step])
         amount         (rf/subscribe [::create-stream/amount])
         token          (rf/subscribe [::create-stream/token])
         active-route   (rf/subscribe [:routes/active])]

     (rf/dispatch [::model/on-submit {:amount "200"
                                      :token {:symbol "BAR"
                                              :address "0xBAR"}}])
     (testing "converts amount to ether"
       (is (= "200000000000000000000" @amount)))

     (testing "adds token contract abi into the database"
       (is (= :fake-abi (:contract-abi @token))))

     (testing "redirects to the amount step"
       (is (= :create-stream/duration @active-route))))))
