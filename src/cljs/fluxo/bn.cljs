(ns fluxo.bn
  (:require ["web3-utils" :as web3-utils]))

(defn to-bn [n]
  (.toBN web3-utils (if (= js/Number (type n)) (str n) n)))
