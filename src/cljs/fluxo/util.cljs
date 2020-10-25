(ns fluxo.util
  (:require [fluxo.money :refer [from-wei]]))

(defn ether->amount [ether]
  (-> ether
      from-wei
      js/parseFloat
      (.toFixed 2)))
