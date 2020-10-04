(ns fluxo.onboarding-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [day8.re-frame.test :as rf-test]
            [re-frame.core :as rf]
            [fluxo.test-helper :refer [fixture-re-frame with-mounted-component found-in]]
            [fluxo.onboarding :as onboarding]))

(use-fixtures :each (fixture-re-frame))

(deftest test-connect-wallet-handler
  (testing "requests permission to access the user's accounts"
    (is (= {:wallet/request {:method "eth_requestAccounts"
                             :on-success [:wallet/accounts-received]
                             :provider nil}}
           (onboarding/connect-wallet-handler {:web3/ethereum nil} nil)))))

(deftest test-fetch-accounts-handler
  (testing "requests user's accounts without asking for permissions"
    (is (= {:wallet/request {:method "eth_accounts"
                             :on-success [:wallet/accounts-received]
                             :provider nil}}
           (onboarding/fetch-accounts-handler {:web3/ethereum nil} nil)))))

(defn noop-fetch-accounts []
  (rf/reg-event-fx
   :onboarding/fetch-accounts
   (fn [_ _])))

(deftest test-onboarding-panel
  (rf-test/run-test-sync
   (noop-fetch-accounts)

   (rf/dispatch [:db/initialize])

   (with-mounted-component [onboarding/onboarding-panel]
     (fn [_ div]
       (is (found-in #"It seems you haven't connected your wallet" div))))

   (rf/dispatch [:wallet/accounts-received ["0x1foo222bar"]])

   (with-mounted-component [onboarding/onboarding-panel]
     (fn [_ div]
       (is (found-in #"Your connected wallet is 0x1fo...bar" div))))))
