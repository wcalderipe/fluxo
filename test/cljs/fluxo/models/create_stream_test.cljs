(ns fluxo.models.create-stream-test
  (:require [cljs.test :refer-macros [testing is use-fixtures]]
            [day8.re-frame.test :refer [run-test-sync]]
            [devcards.core :refer-macros [deftest]]
            #_[fluxo.models.create-stream :as model]
            [fluxo.test-helper :refer [fixture-re-frame]]
            [re-frame.core :as rf]))

;; #_(deftest recipient-test
;;     (run-test-sync
;;      (rf/dispatch [:db/initialize])

;;      (let [recipient (rf/subscribe [::model/recipient])]
;;        (testing "initial state"
;;          (is (= nil @recipient))))))

(deftest foo-test
  (is (= 1 1)))
