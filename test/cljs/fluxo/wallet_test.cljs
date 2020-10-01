(ns fluxo.wallet-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [fluxo.wallet :as wallet]))

(deftest test-mask-address
  (testing "omits the middle characters"
      (is (= (wallet/mask-address "0xfoo0000000000000000000000000000000000bar") "0xfoo...bar"))))
