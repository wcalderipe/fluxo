(ns fluxo.wallet-test
  (:require [cljs.test :refer-macros [testing is async use-fixtures]]
            [devcards.core :refer-macros [deftest]]
            [re-frame.core :as rf]
            [fluxo.test-helper :refer [fixture-re-frame]]
            [fluxo.wallet :as wallet]))

(use-fixtures :each (fixture-re-frame))

(deftest test-mask-address
  (testing "omits the middle characters"
    (is (= (wallet/mask-address "0xfoo0000000000000000000000000000000000bar") "0xfoo...bar"))))

(defn make-fake-provider
  "Make a fake Ethereum provider object within a request method resolving a
  promise."
  []
  (clj->js {:request #(js/Promise.resolve (clj->js {:used-method (.-method %)}))}))

(rf/reg-event-db
 ::good-request-result
 (fn [_ [_ done result]]
   (is (= {:used-method "eth_foo"} result))
   (done)))

(deftest test-request-fx
  (testing "parses result to cljs with keywordize keys and pass it throught"
    (async done
           (wallet/request-fx {:method "eth_foo"
                               :provider (make-fake-provider)
                               :on-success [::good-request-result done]}))))
