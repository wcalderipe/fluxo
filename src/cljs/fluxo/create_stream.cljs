(ns fluxo.create-stream
  (:require [re-frame.core :refer [dispatch reg-event-fx reg-event-db reg-sub subscribe]]
            [reagent.core :as reagent]
            [ajax.core :refer [json-request-format json-response-format]]
            [fluxo.wallet :refer [mask-address]]
            [fluxo.money :refer [from-wei to-wei]]))

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
        [:dispatch [:routes/redirect-to :create-stream/duration]]]})

(reg-event-fx
 :create-stream/on-amount-submit
 on-amount-submit)

(defn add-amount [db [_ amount]]
  (assoc-in db [:create-stream :amount] (to-wei amount)))

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

(reg-sub
 :create-stream/amount-in-wei
 (fn []
   [(subscribe [:create-stream/amount])])
 (fn [[amount]]
   (from-wei amount)))

(defn token [db]
  (get-in db [:create-stream :token]))

(reg-sub
 :create-stream/token
 token)

(defn token-symbol [db]
  (get-in db [:create-stream :token :symbol]))

(reg-sub
 :create-stream/token-symbol
 token-symbol)

(defn- find-by-symbol [symbol]
  (fn [{s :symbol}]
    (= s symbol)))

(defn amount-form [{assets :assets
                    amount :amount}]
  (let [state (reagent/atom {:token  (first assets)
                             :amount amount})]
    (fn []
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (dispatch [:create-stream/on-amount-submit @state]))}
       [:div
        [:select {:on-change #(swap! state assoc :token (filter (find-by-symbol (.. % -target -value)) assets))}
         (for [{token  :token
                name   :name
                symbol :symbol} assets]
           ^{:key (str name symbol)} [:option {:value symbol} name])]]
       [:div
        [:input {:type      :text
                 :value     (:amount @state)
                 :on-change #(swap! state assoc :amount (.. % -target -value))}]]
       [:div
        [:input {:type  :submit
                 :value "Continue"}]]])))

(defn amount-step []
  (let [assets    (subscribe [:wallet/assets])
        recipient (subscribe [:create-stream/recipient])
        amount    (subscribe [:create-stream/amount])]
    (fn []
      [:div
       [:p "How much do you want to sent to " (mask-address @recipient) "?"]
       [amount-form {:assets @assets
                     :amount (from-wei @amount)}]])))

(defn on-duration-submit [_ [_ form-state]]
  {:fx [[:dispatch [:create-stream/add-duration (:duration form-state)]]
        [:dispatch [:routes/redirect-to :create-stream/confirmation]]]})

(reg-event-fx
 :create-stream/on-duration-submit
 on-duration-submit)

(defn add-duration [db [_ duration]]
  (assoc-in db [:create-stream :duration] (js/parseFloat duration)))

(reg-event-db
 :create-stream/add-duration
 add-duration)

(defn duration [db]
  (get-in db [:create-stream :duration]))

(reg-sub
 :create-stream/duration
 duration)

(defn duration-form [defaults]
  (let [state (reagent/atom defaults)]
    (fn []
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (js/console.log @state)
                           (dispatch [:create-stream/on-duration-submit @state]))}
       [:div
        [:input {:type        :text
                 :placeholder "Time in hours"
                 :value       (:duration @state)
                 :on-change   #(swap! state assoc :duration (.. % -target -value))}]]
       [:div
        [:input {:type  :submit
                 :value "Continue"}]]])))

(defn duration-step []
  (let [recipient    (subscribe [:create-stream/recipient])
        token-symbol (subscribe [:create-stream/token-symbol])
        amount       (subscribe [:create-stream/amount-in-wei])
        duration     (subscribe [:create-stream/duration])]
    (fn []
      [:div
       [:p "For how long would you like to stream "
        @token-symbol " " @amount
        " to " (mask-address @recipient)]
       [duration-form {:duration @duration}]])))

(defn on-confirmation [_ [_ stream]]
  (js/console.log stream))

(reg-event-fx
 :create-stream/on-confirmation
 on-confirmation)

(defn confirmation-step []
  (let [sender        (subscribe [:wallet/address])
        recipient     (subscribe [:create-stream/recipient])
        token         (subscribe [:create-stream/token])
        amount        (subscribe [:create-stream/amount])
        amount-in-wei (subscribe [:create-stream/amount-in-wei])
        duration      (subscribe [:create-stream/duration])]
    (fn []
      [:div
       [:p "Review your stream"]
       [:ul
        [:li "From "     (mask-address @sender)]
        [:li "To "       (mask-address @recipient)]
        [:li "Amount "   (:symbol @token) " " @amount-in-wei]
        [:li "Duration " @duration " hours"]]
       [:button {:on-click (fn [e]
                             (.preventDefault e)
                             (dispatch [:create-stream/on-confirmation {:sender    @sender
                                                                        :recipient @recipient
                                                                        :token     @token
                                                                        :amount    @amount
                                                                        :duration  @duration}]))}
        "Confirm"]])))
