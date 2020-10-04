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

     (is (= "0xfoo111bar" @recipient))
     (is (= :create-stream/amount @active-route)))))

(deftest amount-form-submit-test
  (run-test-sync
   (let [asset {:name "Foo" :symbol "FOO" :address "0xfoo111bar"}
         amount (rf/subscribe [:create-stream/amount])
         token (rf/subscribe [:create-stream/token])
         active-route (rf/subscribe [:routes/active])]

     (rf/dispatch [:create-stream/on-amount-submit {:amount "299999999999999980800"
                                                    :token asset}])

     (is (= "299999999999999980800" @amount))
     (is (= asset @token)))))

(deftest amount-step-test
  (run-test-sync
   (rf/dispatch [:db/initialize])
   (rf/dispatch [:create-stream/add-recipient "0xfoo111bar"])

   (with-mounted-component [create-stream/amount-step]
     (fn [_ div]
       (is (found-in #"How much do you want to sent to 0xfoo...bar" div))))))
