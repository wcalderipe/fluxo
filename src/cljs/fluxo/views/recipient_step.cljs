(ns fluxo.views.recipient-step
  (:require [fluxo.models.recipient-step :as model]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(defn recipient-step-component [{:keys [recipient-addr]}]
  (let [state (reagent/atom {:address recipient-addr})]
    (fn []
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (rf/dispatch [::model/on-submit @state]))}
       [:div
        [:label {:for :recipient-addr} "Recipient address"]
        [:input#recipient-addr {:type      :text
                                :value     (:address @state)
                                :required  true
                                ;; Classic Ethereum address regex. This most
                                ;; likely has edge-cases but will work better
                                ;; than nothing for now.
                                :pattern   "^0x[a-fA-F0-9]{40}$"
                                :on-change #(swap! state assoc :address (.. % -target -value))}]]
       [:button {:type :submit} "Continue"]])))

(defn recipient-step []
  (let [model (rf/subscribe [::model/recipient-step])]
    (fn []
      [:section
       [:h1 "What's the Ethereum address or ENS name you want to send money to?"]
       [recipient-step-component @model]])))
