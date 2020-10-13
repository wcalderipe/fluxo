(ns fluxo.views.stream
  (:require [re-frame.core :as rf]
            [fluxo.models.stream :as model]))

(defn- date-time-info [start-time stop-time]
  [:p.date-time-info "It started at " [:strong start-time]
   " and is expected to end at " [:strong stop-time] "."])

(defn stream-component [{:keys [streamed-amount streamed-percentage token-symbol
                                deposit-amount start-time stop-time]}]
  [:div
   [:p "You've streamed " [:strong streamed-amount " (" streamed-percentage "%)"]
    " from a total of " [:strong token-symbol " " deposit-amount] " so far."]
   [date-time-info start-time stop-time]])

(defn stream []
  (let [model (rf/subscribe [::model/stream])]
    (fn []
      [:section#stream
       [:h1 "Streaming..."]
       [stream-component @model]])))
