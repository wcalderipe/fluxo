(ns fluxo.util
  (:require [fluxo.money :refer [from-wei]]))

(defn str->amount [amount]
  (-> amount
      js/parseFloat
      (.toFixed 2)))

(defn ether->amount [ether]
  (-> ether
      from-wei
      str->amount))
