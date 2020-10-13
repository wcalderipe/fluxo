(ns fluxo.models.stream
  (:require [re-frame.core :as rf]
            ["date-fns" :refer [format]]
            ["date-fns/fromUnixTime" :as from-unix-time]
            [fluxo.bn :as bn]
            [fluxo.money :refer [from-wei]]
            [fluxo.epoch :as epoch]
            [fluxo.stream-repository :as stream-repo]))

(def ^:const date-time-format "MM/dd/yyyy @ hh:mm")

(rf/reg-event-db
 ::create
 (fn [db [_ stream]]
   (assoc db :stream stream)))

(rf/reg-sub
 ::start-time
 (fn [db]
   (get-in db [:stream :start-time])))

(rf/reg-sub
 ::stop-time
 (fn [db]
   (get-in db [:stream :stop-time])))

(rf/reg-sub
 ::rate-per-second
 (fn [db]
   (get-in db [:stream :rate-per-second])))

(rf/reg-sub
 ::token-symbol
 (fn [db]
   (get-in db [:stream :token :symbol])))

(rf/reg-sub
 ::streamed-amount
 :<- [::start-time]
 :<- [::rate-per-second]
 (fn [[start-time rate-per-second]]
   (let [now        (js/Math.round (/ (-> (js/Date.) .getTime) 1000))
         time-delta (bn/to-bn (- now start-time))
         rate       (bn/to-bn rate-per-second)]
     (from-wei (.toString (.mul rate time-delta))))))

(rf/reg-sub
 ::deposit-amount
 (fn [db]
   (from-wei (get-in db [:stream :deposit-amount]))))

(rf/reg-sub
 ::streamed-percentage
 :<- [::deposit-amount]
 :<- [::streamed-amount]
 (fn [[deposit-amount streamed-amount]]
   (let [deposit  (.parseFloat js/window deposit-amount)
         streamed (.parseFloat js/window streamed-amount)]
     (* (/ streamed deposit) 100))))

(defn- format-time [time]
  (-> time
      from-unix-time
      (format date-time-format)))

(rf/reg-sub
 ::stream
 :<- [::streamed-amount]
 :<- [::streamed-percentage]
 :<- [::token-symbol]
 :<- [::deposit-amount]
 :<- [::start-time]
 :<- [::stop-time]
 (fn [[streamed-amount streamed-percentage token-symbol
       deposit-amount start-time stop-time]]
   {:streamed-amount     streamed-amount
    :streamed-percentage (js/parseFloat streamed-percentage)
    :token-symbol        token-symbol
    :deposit-amount      (js/parseFloat deposit-amount)
    :start-time          (format-time start-time)
    :stop-time           (format-time stop-time)}))
