(ns fluxo.wallet
  (:require [fluxo.web3 :as web3]
            [re-frame.core :as rf]))

(defn mask-address
  "Omit address middle characters."
  [address]
  (when (not (nil? address))
    (let [start (apply str (take 5 address))
          end   (apply str (take-last 3 address))]
      (str start "..." end))))

(defn listen-accounts-change [^js ethereum]
  (when ethereum
    (.on ethereum "accountsChanged" #(rf/dispatch [:wallet/accounts-received (js->clj %)]))))

(rf/reg-event-fx
 ::get-accounts
 [(rf/inject-cofx :web3/ethereum)]
 (fn [cofx _]
   {:wallet/request {:method     "eth_accounts"
                     :provider   (:web3/ethereum cofx)
                     :on-success [:wallet/accounts-received]}}))

(rf/reg-sub
 :wallet/connected?
 (fn [db]
   (boolean (get-in db [:wallet :address]))))

(rf/reg-sub
 :wallet/address
 (fn [db]
   (get-in db [:wallet :address])))

(defn request-fx [{on-success :on-success
                   method     :method
                   provider   :provider}]
  (-> (.request provider (clj->js {:method method}))
      (.then #(rf/dispatch (conj on-success (js->clj % :keywordize-keys true))))))

(rf/reg-fx
 :wallet/request
 request-fx)

(defn accounts-received-handler [cofx [_ accounts]]
  {:db (-> (:db cofx)
           (assoc-in [:wallet :address] (first accounts)))})

(rf/reg-event-fx
 :wallet/accounts-received
 accounts-received-handler)

(defn add-asset [db [_ {name    :name
                        symbol  :symbol
                        address :address}]]
  (update-in db [:wallet :assets] conj {:name    name
                                        :symbol  symbol
                                        :address address}))

(rf/reg-event-db
 :wallet/add-asset
 add-asset)

(rf/reg-sub
 :wallet/assets
 (fn [db]
   (get-in db [:wallet :assets])))
