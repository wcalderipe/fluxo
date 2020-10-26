(ns fluxo.models.stream
  (:require ["date-fns" :refer [format]]
            ["date-fns/fromUnixTime" :as from-unix-time]
            ["date-fns/isAfter" :as is-after]
            [fluxo.bn :as bn]
            [fluxo.money :refer [from-wei]]
            [fluxo.stream-repository :as stream-repo]
            [fluxo.util :refer [str->amount ether->amount]]
            [re-frame.core :as rf]))

(def ^:const date-time-format "dd/MM/yyyy @ hh:mm")

(rf/reg-event-fx
 ::fetch-stream
 [(rf/inject-cofx ::stream-repo/store)]
 (fn [cofx _]
   (let [stream (cofx ::stream-repo/store)]
     (when stream
       {:db (assoc (:db cofx) :stream stream)}))))

(rf/reg-event-db
 ::create
 (fn [db [_ stream]]
   (assoc db :stream stream)))

(rf/reg-fx
 ::interval
 (let [live-intervals (atom {})]
   (fn [{:keys [action id frequency event]}]
     (if (= action :start)
       (swap! live-intervals assoc id (js/setInterval #(rf/dispatch event) frequency))
       (do (js/clearInterval (get @live-intervals id))
           (swap! live-intervals dissoc id))))))

(rf/reg-event-fx
 ::start-stream-amount-calculation
 (fn [_ _]
   {::interval {:action    :start
                :id        :streamed-calculation
                :frequency 3000
                :event     [::update-streamed-amount]}}))

(rf/reg-event-fx
 ::stop-stream-amount-calculation
 (fn [_ _]
   {::interval {:action :cancel
                :id     :streamed-calculation}}))

(defn- streamed-amount [rate-per-second time-delta]
  (-> (.toString (.mul (bn/to-bn rate-per-second) time-delta))
      from-wei))

(defn- to-float [value]
  (.parseFloat js/window (from-wei value)))

(defn- streamed-percentage [deposit-amount streamed-amount]
  (* (/ streamed-amount deposit-amount) 100))

(defn- time-delta! [start-time]
  (let [now (js/Math.round (/ (-> (js/Date.) .getTime) 1000))]
    (bn/to-bn (- now start-time))))

(rf/reg-event-db
 ::update-streamed-amount
 (fn [db]
   (let [start-time      (get-in db [:stream :start-time] 0)
         rate-per-second (get-in db [:stream :rate-per-second])
         deposit-amount  (get-in db [:stream :deposit-amount])
         amount          (str->amount (streamed-amount rate-per-second (time-delta! start-time)))
         percentage      (.toFixed (streamed-percentage (to-float deposit-amount) amount) 2)]
     (assoc-in db [:stream :streamed] {:amount     amount
                                       :percentage percentage}))))

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
 ::status
 (fn [db]
   (let [now        (js/Date.)
         start-time (from-unix-time (get-in db [:stream :start-time]))
         started?   (is-after now start-time)
         stop-time  (from-unix-time (get-in db [:stream :stop-time]))
         completed? (is-after now stop-time)]
     (cond
       (not started?)                  :not-started
       (and started? (not completed?)) :progress
       (and started? completed?)       :completed
       :else                           :not-started))))

(rf/reg-sub
 ::streamed
 (fn [db]
   (get-in db [:stream :streamed] {:amount     0
                                   :percentage 0})))

(rf/reg-sub
 ::deposit-amount
 (fn [db]
   (from-wei (get-in db [:stream :deposit-amount]))))

(defn- format-time [time]
  (-> time
      from-unix-time
      (format date-time-format)))

(rf/reg-sub
 ::stream
 :<- [::token-symbol]
 :<- [::deposit-amount]
 :<- [::start-time]
 :<- [::stop-time]
 :<- [::status]
 :<- [::streamed]
 (fn [[token-symbol deposit-amount start-time
       stop-time status streamed]]
   {:token-symbol        token-symbol
    :deposit-amount      (str->amount deposit-amount)
    :start-time          (format-time start-time)
    :stop-time           (format-time stop-time)
    :status              status
    :streamed            streamed}))
