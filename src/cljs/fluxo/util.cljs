(ns fluxo.util
  (:require ["web3-utils" :as web3-utils]))

(defn to-wei [amount]
  (when (not (nil? amount))
    (.toWei web3-utils amount)))

(defn from-wei [amount]
  (when (not (nil? amount))
    (.fromWei web3-utils amount)))

(defn str->amount [amount]
  (-> amount
      js/parseFloat
      (.toFixed 2)))

(defn ether->amount [ether]
  (-> ether
      from-wei
      str->amount))
