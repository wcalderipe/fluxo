(ns fluxo.views.duration-step
  (:require [fluxo.models.duration-step :as model]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(defn- form [duration]
  (let [state (reagent/atom {:duration duration})]
    (fn []
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (rf/dispatch [::model/on-submit @state]))}
       [:div
        [:input#duration {:type        :text
                          :value       (:duration @state)
                          :on-change   #(swap! state assoc :duration (.. % -target -value))}]

        [:label {:for :duration} "hours"]]

       [:button {:type :submit} "Continue"]])))

(defn- title [token-symbol amount recipient-addr]
  [:h1 "For how long would you like to stream "
   [:strong token-symbol " " amount] " to " [:strong recipient-addr] "?"])

(defn duration-step-component [{:keys [recipient-addr token-symbol
                                       amount duration]}]
  [:section#duration-step
   [title token-symbol amount recipient-addr]
   [form duration]])

(defn duration-step []
  (let [model (rf/subscribe [::model/duration-step])]
    (fn []
      [duration-step-component @model])))
