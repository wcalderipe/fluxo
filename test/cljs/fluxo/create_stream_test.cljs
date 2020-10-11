(ns fluxo.create-stream-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [cljsjs.react]
            [day8.re-frame.test :refer [run-test-sync]]
            [devcards.core :as dc :refer-macros [defcard]]
            [fluxo.create-stream :as create-stream]
            [fluxo.etherscan :as etherscan]
            [fluxo.test-helper :refer [fixture-re-frame with-mounted-component found-in]]
            [fluxo.wallet :as wallet]
            [re-frame.core :as rf]))

(use-fixtures :each (fixture-re-frame))

(deftest recipient-form-submit-test
  (run-test-sync
   (let [recipient (rf/subscribe [:create-stream/recipient])
         active-route (rf/subscribe [:routes/active])]

     (rf/dispatch [:create-stream/on-recipient-submit {:address "0xfoo111bar"}])

     (testing "adds address as the stream recipient"
       (is (= "0xfoo111bar" @recipient)))

     (testing "redirects to the amount step"
       (is (= :create-stream/amount @active-route))))))

(defcard
  "Recipient Step Component"
  (dc/reagent
   [:div
    [:h3 [:i "Initial state"]]
    [create-stream/recipient-component {:recipient-addr nil}]

    [:h3 [:i "Recipient input filled"]]
    [create-stream/recipient-component {:recipient-addr "0xfoo111bar"}]]))

(defn- stub-etherscan [{expected-params :expected-params
                        response :response}]
  (rf/reg-event-fx
   ::etherscan/get-contract-abi
   (fn [_ [_ params]]
     (is (= expected-params params))
     {:dispatch (conj (:on-success params) response)})))

(deftest amount-form-submit-test
  (run-test-sync
   (stub-etherscan {:expected-params {:address    "0xfoo111bar"
                                      :on-success [:create-stream/on-token-contract-success]}
                    :response        :fake-abi})

   (let [asset {:name "Foo" :symbol "FOO" :address "0xfoo111bar"}
         amount (rf/subscribe [:create-stream/amount])
         token (rf/subscribe [:create-stream/token])
         active-route (rf/subscribe [:routes/active])]

     (rf/dispatch [:create-stream/on-amount-submit {:amount "200"
                                                    :token asset}])

     (testing "converts amount from wei to ether"
       (is (= "200000000000000000000" @amount)))

     (testing "adds token into the database"
       (is (= asset (dissoc @token :contract-abi))))

     (testing "fetches the token contract abi from etherscan"
       (is (= :fake-abi (:contract-abi @token))))

     (testing "redirects to the duration step"
       (is (= :create-stream/duration @active-route))))))

(deftest amount-step-view-test
  (run-test-sync
   (rf/dispatch [:db/initialize])
   (rf/dispatch [:create-stream/add-recipient "0xfoo111bar"])

   (testing "renders recipient address"
     (with-mounted-component [create-stream/amount-step]
       (fn [_ div]
         (is (found-in #"How much do you want to sent to 0xfoo...bar" div)))))

   ;; The add-amount handler converts the value from wei to ether before save
   ;; it.
   (rf/dispatch [:create-stream/add-amount "200"])

   (testing "converts wei amount to a human-readable figure"
     (with-mounted-component [create-stream/amount-step]
       (fn [_ div]
         (is (found-in #"\"200\"" div)))))))

(defcard
  "Amount Step Component"
  (dc/reagent
   [:div
    [:h3 [:i "Initial state"]]
    [create-stream/amount-component {:assets         [{:name "Foo" :symbol "FOO"}
                                                      {:name "Bar" :symbol "BAR"}]
                                     :amount         "200"
                                     :recipient-addr "0xfoo111bar"}]]))

(deftest duration-form-submit-test
  (run-test-sync
   (let [duration (rf/subscribe [:create-stream/duration])
         active-route (rf/subscribe [:routes/active])]

     (rf/dispatch [:create-stream/on-duration-submit {:duration "2"}])

     (testing "converts duration to number"
       (is (= 2 @duration)))

     (testing "redirects to the confirmation step"
       (is (= :create-stream/confirmation @active-route))))))

(deftest duration-step-view-test
  (run-test-sync
   (rf/dispatch [:db/initialize])
   (rf/dispatch [:create-stream/add-recipient "0xfoo111bar"])
   (rf/dispatch [:create-stream/add-amount "200"])
   (rf/dispatch [:create-stream/add-token {:symbol "FOO"}])

   (testing "renders amount and recipient"
     (with-mounted-component [create-stream/duration-step]
       (fn [_ div]
         (is (found-in #"FOO 200 to 0xfoo...bar" div)))))))

(deftest confirmation-test
  (run-test-sync

   (rf/reg-cofx
    :web3/provider
    (fn [cofx]
      (assoc cofx :web3/provider :fake-provider)))

   (rf/reg-fx
    :web3/request-approval
    (fn [{:keys [token-addr token-abi
                 wallet-addr on-success on-failure] :as params}]
      (is (= "fake-token-addr" token-addr))
      (is (= :fake-contract-abi token-abi))
      (is (= "fake-wallet-addr" wallet-addr))
      (is (= :create-stream/on-spend-approve-success (first on-success)))
      (is (= :create-stream/on-spend-approve-failure (first on-failure)))

      (rf/dispatch (conj on-success :success))))

   ;; TODO: Add specs for it
   (rf/reg-fx
    :web3/create-stream (fn [_ _] )) ;; noop

   (rf/dispatch [:db/initialize])
   (rf/dispatch [:wallet/accounts-received ["fake-wallet-addr"]])
   (rf/dispatch [:create-stream/add-recipient "fake-recipient-addr"])
   (rf/dispatch [:create-stream/add-amount "200"])
   (rf/dispatch [:create-stream/add-token {:address "fake-token-addr"}])
   (rf/dispatch [:create-stream/on-token-contract-success :fake-contract-abi])
   (rf/dispatch [:create-stream/add-duration 2])

   (let [db (rf/subscribe [:db/state])
         sender        (rf/subscribe [:wallet/address])
         recipient     (rf/subscribe [:create-stream/recipient])
         token         (rf/subscribe [:create-stream/token])
         amount        (rf/subscribe [:create-stream/amount])
         duration      (rf/subscribe [:create-stream/duration])]

     (rf/dispatch [:create-stream/on-confirmation {:token  @token
                                                   :sender @sender
                                                   :amount @amount}]))))

(deftest confirmation-step-view-test
  (run-test-sync
   (rf/dispatch [:db/initialize])
   (rf/dispatch [:wallet/accounts-received ["0xbar222foo"]])
   (rf/dispatch [:create-stream/add-recipient "0xfoo111bar"])
   (rf/dispatch [:create-stream/add-amount "200"])
   (rf/dispatch [:create-stream/add-token {:symbol "FOO"}])
   (rf/dispatch [:create-stream/add-duration 2])

   (testing "renders information collected in previous steps"
     (with-mounted-component [create-stream/confirmation-step]
       (fn [_ div]
         (is (found-in #"0xbar...foo" div))
         (is (found-in #"0xfoo...bar" div))
         (is (found-in #"FOO 200" div))
         (is (found-in #"2 hours" div)))))))
