(ns fluxo.models.confirmation-step-test
  (:require [cljs.test :refer-macros [testing is use-fixtures]]
            [cljsjs.react]
            [day8.re-frame.test :refer [run-test-sync]]
            [devcards.core :as dc :refer-macros [deftest]]
            [fluxo.models.confirmation-step :as model]
            [fluxo.models.create-stream :as create-stream]
            [fluxo.sablier :as sablier]
            [fluxo.token :as token]
            [fluxo.test-helper :refer [fixture-re-frame]]
            [re-frame.core :as rf]))

(use-fixtures :each (fixture-re-frame))

(deftest confirmation-test
  (run-test-sync

   (rf/reg-cofx
    :web3/provider
    (fn [cofx]
      (assoc cofx :web3/provider :fake-provider)))

   (rf/reg-fx
    ::token/approve
    (fn [{:keys [token-addr token-abi
                 wallet-addr on-success on-failure] :as params}]
      (is (= "fake-token-addr" token-addr))
      (is (= "fake-wallet-addr" wallet-addr))
      (is (= ::model/on-spend-approve-success (first on-success)))
      (is (= ::model/on-spend-approve-failure (first on-failure)))

      (rf/dispatch (conj on-success :success))))

   ;; TODO: Add specs for it
   (rf/reg-fx
    ::sablier/create-stream (fn [_ _]))

   (rf/reg-fx
    ::sablier/get-stream (fn [_ _]))

   (rf/dispatch [:db/initialize])
   (rf/dispatch [:wallet/accounts-received ["fake-wallet-addr"]])
   (rf/dispatch [::create-stream/add-recipient "fake-recipient-addr"])
   (rf/dispatch [::create-stream/add-amount "200000000000000000000"])
   (rf/dispatch [::create-stream/add-token {:address "fake-token-addr" :contract-abi :fake-abi}])
   (rf/dispatch [::create-stream/add-duration 2])

   (let [db (rf/subscribe [:db/state])
         sender        (rf/subscribe [:wallet/address])
         recipient     (rf/subscribe [::create-stream/recipient-addr])
         token         (rf/subscribe [::create-stream/token])
         amount        (rf/subscribe [::create-stream/amount])
         duration      (rf/subscribe [::create-stream/duration])]

     (rf/dispatch [::model/on-submit {:token  @token
                                      :sender @sender
                                      :amount @amount}]))))
