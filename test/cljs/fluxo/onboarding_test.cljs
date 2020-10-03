(ns fluxo.onboarding-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [fluxo.onboarding :as onboarding]))

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
