(ns fluxo.create-stream
  (:require [re-frame.core :refer [dispatch reg-event-fx reg-event-db reg-sub subscribe]]
            [reagent.core :as reagent]))

(defn on-recipient-submit [_ [_ form-state]]
  {:fx [[:dispatch [:create-stream/add-recipient (:address form-state)]]
        [:dispatch [:routes/redirect-to :home]]]}) ; TODO: change me

(reg-event-fx
 :create-stream/on-recipient-submit
 on-recipient-submit)

(defn add-recipient [db [_ recipient]]
  (assoc-in db [:create-stream :recipient] recipient))

(reg-event-db
 :create-stream/add-recipient
 add-recipient)

(defn recipient [db]
  (get-in db [:create-stream :recipient]))

(reg-sub
 :create-stream/recipient
 recipient)

(defn recipient-form [default-address]
  (let [state (reagent/atom {:address default-address})]
    (fn []
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (dispatch [:create-stream/on-recipient-submit @state]))}
       [:div
        [:input {:type :text
                 :value (:address @state)
                 :on-change #(swap! state assoc :address (.. % -target -value))}]]
       [:div
        [:input {:type :submit
                 :value "Continue"}]]])))

(defn recipient-step []
  (let [recipient (subscribe [:create-stream/recipient])]
    (fn []
      [:div
       [:p "What's the Ethereum address or ENS name you want to send money to?"]
       [recipient-form @recipient]])))
