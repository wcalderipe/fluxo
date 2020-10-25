(ns fluxo.models.onboarding-test
  (:require [cljs.test :refer-macros [async testing is use-fixtures]]
            [cljsjs.react]
            [day8.re-frame.test :refer [run-test-sync]]
            [devcards.core :as dc :refer-macros [deftest]]
            [fluxo.models.onboarding :as model]
            [fluxo.web3 :as web3]
            [fluxo.test-helper :refer [fixture-re-frame]]
            [re-frame.core :as rf]))

(use-fixtures :each (fixture-re-frame))

(defn- stub-web3-provider []
  (rf/reg-cofx
   :web3/ethereum
   (fn [cofx]
     (assoc cofx :web3/ethereum :fake-ethereum))))

(deftest initial-state-test
  (run-test-sync
   (stub-web3-provider)
   (rf/dispatch [:db/initialize])

   (let [onboarding (rf/subscribe [::model/onboarding])]
     (is (= {:wallet-connected? false
             :wallet-addr       nil
             :ethereum?         false} @onboarding)))))

(defn- stub-ethereum-presence [{:keys [present?]}]
  (rf/reg-event-db
   ::web3/save-ethereum-presence
   (fn [db]
     (assoc-in db [:web3 :ethereum-present?] present?))))

(deftest connected-wallet-test
  (run-test-sync
   (stub-web3-provider)

   (rf/dispatch [:db/initialize])

   (rf/reg-fx
    :wallet/request
    (fn [{:keys [provider method on-success]}]
      (testing "requests user's accounts"
        (is (= "eth_requestAccounts" method)))

      (testing "dispatches to wallet accounts received"
        (is (= :wallet/accounts-received (first on-success))))

      (rf/dispatch [:wallet/accounts-received ["0xfoo111222bar"]])))

   (let [onboarding (rf/subscribe [::model/onboarding])]
     (rf/dispatch [::model/connect-wallet])

     (is (= {:wallet-connected? true
             :wallet-addr       "0xfoo111222bar"
             :ethereum?         false} @onboarding)))))
