(ns fluxo.create-stream
  (:require [re-frame.core :refer [dispatch reg-event-fx reg-event-db reg-sub subscribe]]
            [reagent.core :as reagent]
            [fluxo.wallet :refer [mask-address]]))

(defn on-recipient-submit [_ [_ form-state]]
  {:fx [[:dispatch [:create-stream/add-recipient (:address form-state)]]
        [:dispatch [:routes/redirect-to :create-stream/amount]]]})

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

(defn on-amount-submit [_ [_ form-state]]
  {:fx [[:dispatch [:create-stream/add-amount (:amount form-state)]]
        [:dispatch [:create-stream/add-token (:token form-state)]]
        #_[:dispatch [:routes/redirect-to :home]]]})

(reg-event-fx
 :create-stream/on-amount-submit
 on-amount-submit)

(defn add-amount [db [_ amount]]
  (assoc-in db [:create-stream :amount] amount))

(reg-event-db
 :create-stream/add-amount
 add-amount)

(defn add-token [db [_ token]]
  (assoc-in db [:create-stream :token] token))

(reg-event-db
 :create-stream/add-token
 add-token)

(defn amount [db]
  (get-in db [:create-stream :amount]))

(reg-sub
 :create-stream/amount
 amount)

(defn token [db]
  (get-in db [:create-stream :token]))

(reg-sub
 :create-stream/token
 token)

(defn amount-form [{assets :assets
                    amount :amount}]
  (let [state (reagent/atom {:token (first assets)
                             :amount amount})]
    (fn []
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (dispatch [:create-stream/on-amount-submit @state]))}
       [:div
        [:input {:type :text
                 :value (:amount @state)
                 :on-change #(swap! state assoc :amount (.. % -target -value))}]]
       [:div
        [:input {:type :submit
                 :value "Continue"}]]])))

(defn amount-step []
  (let [assets (subscribe [:wallet/assets])
        recipient (subscribe [:create-stream/recipient])]
    (fn []
      [:div
       [:p "How much do you want to sent to " (mask-address @recipient) "?"]
       [amount-form {:assets @assets}]])))
