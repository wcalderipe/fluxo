(ns fluxo.views.stream
  (:require [re-frame.core :as rf]
            [fluxo.models.stream :as model]))

(defn- title [status]
  (cond
    (= status :not-started) "Your stream is about to start"
    (= status :progress)    "Streaming"
    (= status :completed)   "Your stream is completed"))

(defn- not-started [{:keys [start-time]}]
  [:div
   [:p "Your stream will start at " [:strong start-time]]])

(defn- date-time-info [start-time stop-time]
  [:p.date-time-info "It started at " [:strong start-time]
   " and is expected to end at " [:strong stop-time] "."])

(defn- progress [{:keys [streamed-amount streamed-percentage token-symbol
                         deposit-amount start-time stop-time]}]
  [:div
   [:p "You've streamed " [:strong streamed-amount " (" streamed-percentage "%)"]
    " from a total of " [:strong token-symbol " " deposit-amount] " so far."]
   [date-time-info start-time stop-time]])

(defn- completed []
  [:div
   [:p "You've streamed 100% of the initial amount."]])

;; TODO: Add controls to withdraw the stream at any moment.
(defn stream-component [{:keys [status] :as model}]
  [:section#stream
   [:h1 (title status)]
   [:div
    (cond
      (= status :not-started) [not-started model]
      (= status :progress)    [progress model]
      (= status :completed)   [completed])]])

(defn stream []
  (let [model (rf/subscribe [::model/stream])]
    (fn []
      (js/console.log @model)
      [stream-component @model])))
