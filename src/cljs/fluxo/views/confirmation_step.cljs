(ns fluxo.views.confirmation-step
  (:require [fluxo.models.confirmation-step :as model]
            [fluxo.wallet :refer [mask-address]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(defn confirmation-step-component [{:keys [wallet-addr recipient-addr token
                                           amount ether-amount duration]}]
  [:section#confirmation-step
   [:h1 "Review your stream"]
   [:ul
    [:li [:span.label "From"]     [:span.value (mask-address wallet-addr)]]
    [:li [:span.label "To"]       [:span.value (mask-address recipient-addr)]]
    [:li [:span.label "Amount"]   [:span.value (:symbol token) " " amount]]
    [:li [:span.label "Duration"] [:span.value duration " hours"]]]
   [:button {:on-click (fn [e]
                         (.preventDefault e)
                         (rf/dispatch [::model/on-submit {:sender    wallet-addr
                                                          :recipient recipient-addr
                                                          :token     token
                                                          :amount    ether-amount
                                                          :duration  duration}]))} "Confirm"]])

(defn confirmation-step []
  (let [model (rf/subscribe [::model/confirmation-step])]
    (fn []
      [confirmation-step-component @model])))
