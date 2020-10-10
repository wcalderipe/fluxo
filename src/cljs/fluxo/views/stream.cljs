(ns fluxo.views.stream
  (:require [re-frame.core :as rf]
            [fluxo.models.stream :as model]))

(defn stream-component [{:keys [streamed-amount streamed-percentage token-symbol
                                deposit-amount start-time stop-time]}]
  [:div
   [:p "You've streamed " streamed-amount " (" streamed-percentage "%)"
    " from a total of " token-symbol " " deposit-amount " so far."]
   [:p "Start time: " (str start-time)]
   [:p "Stop time: " (str stop-time)]])

(defn stream []
  (let [model (rf/subscribe [::model/stream])]
    (fn []
      [:section#stream-details
       [:h1 "Streaming..."]
       [stream-component @model]])))
