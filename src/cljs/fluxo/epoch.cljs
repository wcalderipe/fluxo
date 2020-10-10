(ns fluxo.epoch)

(defn now! []
  (/ (js/Math.round (/ (-> (js/Date.) .getTime))) 1000))

(defn ->inst [timestamp]
  (let [epoch-date (js/Date. 0)]
    (.setUTCSeconds epoch-date timestamp)
    epoch-date))

(defn inst->epoch [date]
  (/ (.getTime date) 1000))
