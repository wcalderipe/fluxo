(ns fluxo.views.add-token
  (:require [fluxo.models.add-token :as model]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(defn- form []
  (let [state (reagent/atom {})]
    (fn []
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (rf/dispatch [::model/on-submit @state]))}
       [:div.controls
        [:label {:for :token-addr} "Token contract address"]
        [:input#token-addr {:type        :text
                            :placeholder "Address"
                            :required    true
                            :pattern     "^0x[a-fA-F0-9]{40}$"
                            :value       (:address @state)
                            :on-change   #(swap! state assoc :address (.. % -target -value))}]

        [:label {:for :token-addr} "Symbol"]
        [:input#token-addr {:type        :text
                            :placeholder "Symbol"
                            :required    true
                            :value       (:symbol @state)
                            :on-change   #(swap! state assoc :symbol (.. % -target -value))}]]

       [:button {:type :submit} "Save token"]])))

(defn add-token-component []
  [:section#add-token
   [:h1 "Add a new token"]
   [form]])

(defn add-token []
  [add-token-component])
