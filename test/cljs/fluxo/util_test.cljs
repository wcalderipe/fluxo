(ns fluxo.util-test
  (:require [cljs.test :refer-macros [testing is]]
            [devcards.core :refer-macros [deftest]]
            [fluxo.util :as util]))

(deftest to-wei-test
  (testing "does not throw if amount is nil"
    (is (= nil (util/to-wei nil)))))

(deftest from-wei-test
  (testing "does not throw if amount is nil"
    (is (= nil (util/from-wei nil)))))
