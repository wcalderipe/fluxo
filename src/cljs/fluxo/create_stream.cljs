(ns fluxo.create-stream
  (:require-macros [fluxo.resources :refer [inline]])
  (:require [ajax.core :refer [json-request-format json-response-format]]
            [fluxo.money :refer [from-wei to-wei]]
            [fluxo.wallet :refer [mask-address]]
            [fluxo.models.create-stream :as model]
            [fluxo.etherscan :as etherscan]
            [re-frame.core :refer [dispatch inject-cofx reg-event-db reg-event-fx reg-sub subscribe]]
            [reagent.core :as reagent]))

(defn on-token-contract-success [db [_ response]]
  (assoc-in db [:create-stream :token :contract-abi] response))

(reg-event-db
 :create-stream/on-token-contract-success
 on-token-contract-success)

(defn on-amount-submit [_ [_ form-state]]
  {:fx [[:dispatch [:create-stream/add-amount (:amount form-state)]]
        [:dispatch [:create-stream/add-token (:token form-state)]]
        ;; TODO: Why not an effect instead?
        [:dispatch [::etherscan/get-contract-abi {:address    (get-in form-state [:token :address])
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

(defn amount-form [{:keys [assets amount recipient-addr]}]
  (let [state (reagent/atom {:token  (first assets)
                             :amount amount})]
    (fn []
      [:form#amount-step {:on-submit (fn [e]
                                       (.preventDefault e)
                                       (dispatch [:create-stream/on-amount-submit @state]))}
       [:div
        [:label {:for :token} "Select an asset token"]
        [:select#token {:on-change #(swap! state assoc :token (filter (find-by-symbol (.. % -target -value)) assets))}
         (for [{token  :token
                symbol :symbol} assets]
           ^{:key (str name symbol)} [:option {:value symbol} symbol])]

        [:label {:for :amount} "Enter an amount"]
        [:input#amount {:type      :text
                        :value     (:amount @state)
                        :on-change #(swap! state assoc :amount (.. % -target -value))}]]
       [:button {:type :submit} "Continue"]])))

(defn amount-component [model]
  [:section
   [:h1 "How much do you want to sent to " (mask-address (:recipient-addr model)) "?"]
   [amount-form model]])

(defn amount-step []
  (let [assets    (subscribe [:wallet/assets])
        recipient (subscribe [::model/recipient-addr])
        amount    (subscribe [:create-stream/amount])]
    (fn []
      [amount-component {:assets         @assets
                         :amount         (from-wei @amount)
                         :recipient-addr @recipient}])))

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

(defn duration-form [{:keys [duration token-symbol amount recipient-addr]}]
  (let [state (reagent/atom {:duration duration})]
    (fn []
      [:form {:on-submit (fn [e]
                           (.preventDefault e)
                           (js/console.log @state)
                           (dispatch [:create-stream/on-duration-submit @state]))}
       [:div
        [:label {:for :duration} "Duration in hours"]
        [:input#duration {:type        :text
                          :value       (:duration @state)
                          :on-change   #(swap! state assoc :duration (.. % -target -value))}]]
       [:button {:type :submit} "Continue"]])))

(defn duration-step []
  (let [recipient-addr (subscribe [::model/recipient-addr])
        token-symbol   (subscribe [:create-stream/token-symbol])
        amount         (subscribe [:create-stream/amount-in-wei])
        duration       (subscribe [:create-stream/duration])]
    (fn []
      [:section
       [:h1 "For how long would you like to stream "
        @token-symbol " " @amount
        " to " (mask-address @recipient-addr)]
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
 [(inject-cofx :web3/provider)]
 (fn [cofx [_ stream result]]
   (js/console.log "Spend approved:" result)
   (js/console.log "Stream:" stream)
   {:web3/create-stream {:provider       (:web3/provider cofx)
                         :token-addr     (get-in stream [:token :address])
                         :wallet-addr    (.. result -from)
                         :recipient-addr (:recipient stream)
                         :amount         (:amount stream)
                         :duration       (:duration stream)
                         :on-success     [::on-create-stream stream]
                         :on-failure     [::on-create-stream]}}))
(reg-event-fx
 :create-stream/on-spend-approve-failure
 (fn [cofx [_ reason]]
   (js/console.warn "Spend approval declined by the user:" reason)
   {:db (assoc (:db cofx) :loading? false)}))

(defn- get-returned-values [^js response]
  (.. response -events -CreateStream -returnValues))

(defn- make-stream [^js returned-values]
  {:id              (.. returned-values -streamId)
   :deposit-amount  (.. returned-values -deposit)
   :sender-addr     (.. returned-values -sender)
   :recipient-addr  (.. returned-values -recipient)
   :start-time      (js/parseInt (.. returned-values -startTime))
   :stop-time       (js/parseInt (.. returned-values -stopTime))})

(reg-event-fx
 ::on-create-stream
 [(inject-cofx :web3/provider)]
 (fn [cofx [_ create-stream response]]
   (js/console.log response)
   (let [stream (make-stream (get-returned-values response))]
     {:db (-> (:db cofx)
              (assoc :loading? false)
              (assoc :stream (merge stream {:token (:token create-stream)})))
      :web3/get-stream {:provider  (:web3/provider cofx)
                        :stream-id (:id stream)
                        :wallet-addr (:sender-addr stream)
                        :on-success [::on-get-stream-success]}})))

(reg-event-fx
 ::on-get-stream-success
 (fn [cofx [_ stream]]
   {:db (assoc-in (:db cofx) [:stream :rate-per-second] (.. stream -ratePerSecond))
    :dispatch [:routes/redirect-to :stream/details]}))

(defn confirmation-step []
  (let [sender        (subscribe [:wallet/address])
        recipient     (subscribe [::model/recipient-addr])
        token         (subscribe [:create-stream/token])
        amount        (subscribe [:create-stream/amount])
        amount-in-wei (subscribe [:create-stream/amount-in-wei])
        duration      (subscribe [:create-stream/duration])]
    (fn []
      [:section#confirmation-step
       [:h1 "Review your stream"]
       [:ul
        [:li [:span.label "From"]     [:span.value (mask-address @sender)]]
        [:li [:span.label "To"]       [:span.value (mask-address @recipient)]]
        [:li [:span.label "Amount"]   [:span.value (:symbol @token) " " @amount-in-wei]]
        [:li [:span.label "Duration"] [:span.value @duration " hours"]]]
       [:button {:on-click (fn [e]
                             (.preventDefault e)
                             (dispatch [:create-stream/on-confirmation {:sender    @sender
                                                                        :recipient @recipient
                                                                        :token     @token
                                                                        :amount    @amount
                                                                        :duration  @duration}]))}
        "Confirm"]])))
