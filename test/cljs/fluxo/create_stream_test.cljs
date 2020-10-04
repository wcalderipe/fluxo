(ns fluxo.create-stream-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [day8.re-frame.test :refer [run-test-sync]]
            [re-frame.core :as rf]
            [fluxo.test-helper :refer [fixture-re-frame with-mounted-component found-in]]
            [fluxo.create-stream :as create-stream]))

(deftest recipient-form-submit-test
  (run-test-sync
   (let [recipient (rf/subscribe [:create-stream/recipient])
         active-route (rf/subscribe [:routes/active])]

     (rf/dispatch [:create-stream/on-recipient-submit {:address "0xfoo111bar"}])

     (testing "adds address as the stream recipient"
       (is (= "0xfoo111bar" @recipient)))

     (testing "redirects to the amount step"
       (is (= :create-stream/amount @active-route))))))

(deftest amount-form-submit-test
  (run-test-sync
   (let [asset {:name "Foo" :symbol "FOO" :address "0xfoo111bar"}
         amount (rf/subscribe [:create-stream/amount])
         token (rf/subscribe [:create-stream/token])
         active-route (rf/subscribe [:routes/active])]

     (rf/dispatch [:create-stream/on-amount-submit {:amount "200"
                                                    :token asset}])

     (testing "converts amount from wei to ether"
       (is (= "200000000000000000000" @amount)))

     (testing "adds token into the database"
       (is (= asset @token)))

     (testing "redirects to the duration step"
       (is (= :create-stream/duration @active-route))))))

(deftest amount-step-test
  (run-test-sync
   (rf/dispatch [:db/initialize])
   (rf/dispatch [:create-stream/add-recipient "0xfoo111bar"])

   (testing "renders recipient address"
     (with-mounted-component [create-stream/amount-step]
       (fn [_ div]
         (is (found-in #"How much do you want to sent to 0xfoo...bar" div)))))

   ;; The add-amount event converts the value from wei to ether before saving.
   (rf/dispatch [:create-stream/add-amount "200"])

   (testing "converts wei amount to a human-readable figure"
     (with-mounted-component [create-stream/amount-step]
       (fn [_ div]
         (is (found-in #"\"200\"" div)))))))

(deftest duration-form-submit-test
  (run-test-sync
   (let [duration (rf/subscribe [:create-stream/duration])
         active-route (rf/subscribe [:routes/active])]

     (rf/dispatch [:create-stream/on-duration-submit {:duration "2"}])

     (testing "converts duration to number"
       (is (= 2 @duration)))

     (testing "redirects to the confirmation step"
       (is (= :create-stream/duration @active-route))))))

(deftest duration-step-test
  (run-test-sync
   (rf/dispatch [:db/initialize])
   (rf/dispatch [:create-stream/add-recipient "0xfoo111bar"])
   (rf/dispatch [:create-stream/add-amount "200"])
   (rf/dispatch [:create-stream/add-token {:symbol "FOO"}])

   (testing "renders amount and recipient"
     (with-mounted-component [create-stream/duration-step]
       (fn [_ div]
         (is (found-in #"FOO 200 to 0xfoo...bar" div)))))))
