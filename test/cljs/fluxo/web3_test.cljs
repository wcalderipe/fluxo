(ns fluxo.web3-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [fluxo.web3 :as web3]))

(deftest test-request-accounts
  (testing "calls on-success when promise is resolved"
    (let [fake-ethereum (clj->js {:request #(js/Promise.resolve (clj->js ["0xfoo1bar"]))})]
      (async done
             (web3/request-accounts fake-ethereum (fn [addrs]
                                                    (is (= ["0xfoo1bar"] addrs))
                                                    (done))))))

  (testing "calls on-failure when promise is rejected"
    (let [fake-ethereum (clj->js {:request #(js/Promise.reject :oh-snap)})]
      (async done
             (web3/request-accounts fake-ethereum nil (fn [err]
                                                        (is (= :oh-snap err))
                                                        (done)))))))
