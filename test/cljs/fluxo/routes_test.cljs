(ns fluxo.routes-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [fluxo.routes :as routes]))

(deftest test-redirect-to-handler
  (testing "sets :active-panel keyword in the db"
    (is (= {:routes {:active :foo}} (routes/redirect-to-handler {} [nil :foo])))))

(deftest test-get-active-route-handler
  (testing "gets :active-panel keyword from the db"
    (is (= :foo (routes/get-active-route-handler {:routes {:active :foo}})))))
