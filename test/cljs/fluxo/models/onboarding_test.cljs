(ns fluxo.models.onboarding-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [day8.re-frame.test :refer [run-test-sync]]
            [fluxo.models.onboarding :as model]
            [fluxo.test-helper :refer [fixture-re-frame]]
            [re-frame.core :as rf]))

(use-fixtures :each (fixture-re-frame))

(defn- stub-wallet-request [expected-method expected-on-success]
  (rf/reg-fx
   :wallet/request
   (fn [{:keys [provider method on-success]}]
     (testing "requests user's accounts"
       (is (= expected-method method)))
     (testing "dispatches to wallet accounts received"
       (is (= expected-on-success (first on-success)))))))

(deftest connecting-to-wallet-test
  (run-test-sync
   (stub-wallet-request "eth_requestAccounts" :wallet/accounts-received)

   (rf/dispatch [::model/connect-wallet])))

(deftest initial-state-test
  (run-test-sync
   (rf/dispatch [:db/initialize])

   (let [onboarding (rf/subscribe [::model/onboarding])]
     (testing "initial state"
       (is (= {:wallet-connected? false
               :wallet-addr       nil} @onboarding))))))

(deftest connected-wallet-test
  (run-test-sync
   (rf/dispatch [:db/initialize])
   (rf/dispatch [:wallet/accounts-received ["0xfoo111222bar"]])

   (let [onboarding (rf/subscribe [::model/onboarding])]
     (testing "user has connected their wallet"
       (is (= {:wallet-connected? true
               :wallet-addr       "0xfoo...bar"} @onboarding))))))
