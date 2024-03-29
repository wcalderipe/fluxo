(ns fluxo.models.confirmation-step
  (:require-macros [fluxo.resources :refer [inline]])
  (:require [fluxo.models.create-stream :as create-stream]
            [fluxo.sablier :as sablier]
            [fluxo.stream-repository :as stream-repo]
            [fluxo.token :as token]
            [fluxo.util :refer [ether->amount from-wei]]
            [fluxo.wallet :refer [mask-address]]
            [re-frame.core :as rf]))

(defonce sablier-ropsten {:address      "0xc04Ad234E01327b24a831e3718DBFcbE245904CC"
                          :contract-abi (.parse js/JSON (inline "ropsten_sablier-contract-abi.json"))})

(rf/reg-event-fx
 ::on-submit
 [(rf/inject-cofx :web3/provider)]
 (fn [cofx [_ {:keys [token sender amount] :as stream}]]
   {:db             (assoc (:db cofx) :loading? true)
    ::token/approve {:provider     (:web3/provider cofx)
                     :token-addr   (:address token)
                     :spender-addr (:address sablier-ropsten)
                     :wallet-addr  sender
                     :amount       amount
                     :on-success   [::on-spend-approve-success stream]
                     :on-failure   [::on-spend-approve-failure]}}))

(rf/reg-event-fx
 ::on-spend-approve-success
 [(rf/inject-cofx :web3/provider)]
 (fn [cofx [_ stream result]]
   {::sablier/create-stream {:provider       (:web3/provider cofx)
                             :token-addr     (get-in stream [:token :address])
                             :wallet-addr    (.. result -from)
                             :recipient-addr (:recipient stream)
                             :amount         (:amount stream)
                             :duration       (:duration stream)
                             :on-success     [::on-create-stream stream]
                             :on-failure     [::on-create-stream]}}))

(rf/reg-event-fx
 ::on-spend-approve-failure
 (fn [cofx [_ reason]]
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

(rf/reg-event-fx
 ::on-create-stream
 [(rf/inject-cofx :web3/provider)]
 (fn [cofx [_ create-stream response]]
   (let [stream (make-stream (get-returned-values response))]
     {:db (-> (:db cofx)
              (assoc :loading? false)
              (assoc :stream (merge stream {:token (:token create-stream)})))
      :fx [[:dispatch [::sablier/get-stream {:stream-id   (:id stream)
                                             :on-success  [::on-get-stream-success]}]]]})))

(rf/reg-event-fx
 ::on-get-stream-success
 (fn [cofx [_ response]]
   (let [rate-per-sec (get-in response [:data :stream :ratePerSecond])
         db           (-> (:db cofx)
                          (assoc-in [:stream :rate-per-second] rate-per-sec)
                          (dissoc :create-stream))]
     {:db                 db
      ::stream-repo/write (:stream db)
      :dispatch           [:routes/redirect-to :stream/details]})))

(rf/reg-sub
 ::loading?
 (fn [db]
   (get db :loading? false)))

(rf/reg-sub
 ::confirmation-step
 :<- [:wallet/address]
 :<- [::create-stream/recipient-addr]
 :<- [::create-stream/token]
 :<- [::create-stream/amount]
 :<- [::create-stream/duration]
 :<- [::loading?]
 (fn [[wallet-addr recipient-addr token amount duration loading?]]
   {:wallet-addr    wallet-addr
    :recipient-addr recipient-addr
    :token          token
    :ether-amount   amount
    :amount         (ether->amount amount)
    :duration       duration
    :loading?       loading?}))
