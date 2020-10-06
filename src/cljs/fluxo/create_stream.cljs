(ns fluxo.create-stream
  (:require-macros [fluxo.resources :refer [inline]])
  (:require [ajax.core :refer [json-request-format json-response-format]]
            [devcards.core :as dc]
            [fluxo.money :refer [from-wei to-wei]]
            [fluxo.wallet :refer [mask-address]]
            [re-frame.core :refer [dispatch inject-cofx reg-event-db reg-event-fx reg-sub subscribe]]
            [reagent.core :as reagent]))

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

(defn recipient-form [recipient-addr]
  (let [state (reagent/atom {:address recipient-addr})]
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

(defn recipient-component [model]
  [:div
   [:p "What's the Ethereum address or ENS name you want to send money to?"]
   [recipient-form (:recipient model)]])

(dc/defcard "Recipient Component"
  (dc/reagent [recipient-component {:recipient "0xfoo111bar"}]))

(defn recipient-step []
  (let [recipient (subscribe [:create-stream/recipient])]
    (fn []
      [recipient-component {:recipient @recipient}])))

(defn on-token-contract-success [db [_ response]]
  (assoc-in db [:create-stream :token :contract-abi] response))

(reg-event-db
 :create-stream/on-token-contract-success
 on-token-contract-success)

(defn on-amount-submit [_ [_ form-state]]
  {:fx [[:dispatch [:create-stream/add-amount (:amount form-state)]]
        [:dispatch [:create-stream/add-token (:token form-state)]]
        ;; TODO: Why not an effect instead?
        [:dispatch [:etherscan/get-contract-abi {:address    (get-in form-state [:token :address])
                                                 :on-success [:create-stream/on-token-contract-success]}]]
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

(defonce sablier-ropsten {:address      "0xc04Ad234E01327b24a831e3718DBFcbE245904CC"
                          :contract-abi (.parse js/JSON (inline "ropsten_sablier-contract-abi.json"))})

(reg-event-fx
 :create-stream/on-confirmation
 [(inject-cofx :web3/provider)]
 (fn [cofx [_ {:keys [token sender amount] :as stream}]]
   {:db                    (assoc (:db cofx) :loading? true)

    :web3/request-approval {:provider     (:web3/provider cofx)
                            :token-addr   (:address token)
                            :token-abi    (:contract-abi token)
                            :spender-addr (:address sablier-ropsten)
                            :wallet-addr  sender
                            :amount       amount
                            :on-success   [:create-stream/on-spend-approve-success stream]
                            :on-failure   [:create-stream/on-spend-approve-failure]}}))

(reg-event-fx
 :create-stream/on-spend-approve-success
 (fn [cofx [_ stream result]]
   (js/console.log "Spend approved:" result)
   (js/console.log "Stream:" stream)
   {:db (assoc (:db cofx) :loading? false)}))

(reg-event-fx
 :create-stream/on-spend-approve-failure
 (fn [cofx [_ reason]]
   (js/console.warn "Spend approval declined by the user:" reason)
   {:db (assoc (:db cofx) :loading? false)}))

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
