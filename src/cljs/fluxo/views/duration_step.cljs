(ns fluxo.views.duration-step
  (:require [fluxo.models.duration-step :as model]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(defn- form [duration]
  (let [state (reagent/atom {:duration duration})]
    (fn []
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (js/console.log @state)
                           (rf/dispatch [::model/on-submit @state]))}
       [:div
        [:label {:for :duration} "Duration in hours"]
        [:input#duration {:type        :text
                          :value       (:duration @state)
                          :on-change   #(swap! state assoc :duration (.. % -target -value))}]]
       [:button {:type :submit} "Continue"]])))

(defn- make-title [token-symbol amount recipient-addr]
  (str "For how long would you like to stream " token-symbol " " amount " to " recipient-addr))

(defn duration-step-component [{:keys [recipient-addr token-symbol
                                       amount duration]}]
  [:section
   [:h1 (make-title token-symbol amount recipient-addr)]
   [form duration]])

(defn duration-step []
  (let [model (rf/subscribe [::model/duration-step])]
    (fn []
      [duration-step-component @model])))
