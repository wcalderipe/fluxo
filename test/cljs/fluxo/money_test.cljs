(ns fluxo.money-test
  (:require [cljs.test :refer-macros [testing is]]
            [devcards.core :refer-macros [deftest]]
            [fluxo.money :as money]))

(deftest to-wei-test
  (testing "does not throw if amount is nil"
    (is (= nil (money/to-wei nil)))))

(deftest from-wei-test
  (testing "does not throw if amount is nil"
    (is (= nil (money/from-wei nil)))))
