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
     (is (= :home @active-route)))))
