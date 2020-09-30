(ns fluxo.routes-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [fluxo.routes :as routes]))

(deftest test-set-active-panel-handler
  (testing "sets :active-panel keyword in the db"
    (is (= {:active-panel :foo} (routes/set-active-panel-handler {} [nil :foo])))))

(deftest test-get-active-panel
  (testing "gets :active-panel keyword from the db"
    (is (= :foo (routes/get-active-panel {:active-panel :foo})))))
