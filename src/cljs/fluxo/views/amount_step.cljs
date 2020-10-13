(ns fluxo.views.amount-step
  (:require [fluxo.models.amount-step :as model]
            [reagent.core :as reagent]
            [re-frame.core :as rf]))

(defn- find-by-symbol [symbol]
  (fn [{s :symbol}]
    (= s symbol)))

(defn amount-form [{:keys [assets amount]}]
  (let [state (reagent/atom {:token  (first assets)
                             :amount amount})]
    (fn []
      [:form#amount-step {:on-submit (fn [e]
                                       (.preventDefault e)
                                       (rf/dispatch [::model/on-submit @state]))}
       [:div
        [:label {:for :token} "Select an asset token"]
        [:select#token {:on-change #(swap! state assoc :token (filter (find-by-symbol (.. % -target -value)) assets))}
         (for [{token  :token
                symbol :symbol} assets]
           ^{:key (str name symbol)} [:option {:value symbol} symbol])]

        [:label {:for :amount} "Enter an amount"]
        [:input#amount {:type      :text
                        :required  true
                        :value     (:amount @state)
                        :on-change #(swap! state assoc :amount (.. % -target -value))}]]

       [:button {:type :submit} "Continue"]])))

(defn amount-step-component [model]
  [:section#amount-step
   [:h1 "How much do you want to sent to " (:recipient-addr model) "?"]
   [amount-form model]])

(defn amount-step []
  (let [model (rf/subscribe [::model/amount-step])]
    (fn []
      [amount-step-component @model])))
