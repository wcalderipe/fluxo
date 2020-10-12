(ns fluxo.stream-repository
  (:require [re-frame.core :as rf]))

(defn write! [stream]
  (->> (.stringify js/JSON (clj->js stream))
       (.setItem js/window.localStorage "fluxo.stream")))

(defn read! []
  (->> (.getItem js/window.localStorage "fluxo.stream")
       (.parse js/JSON)
       (#(js->clj % :keywordize-keys true))))

(rf/reg-fx
 ::write
 (fn [stream]
   (write! stream)))

(rf/reg-fx
 ::read
 (fn [{:keys [on-success]}]
   (rf/dispatch (conj on-success read!))))
