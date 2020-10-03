(ns fluxo.wallet
  (:require [re-frame.core :refer [reg-sub reg-fx reg-event-fx dispatch]]
            [fluxo.web3 :as web3]))

(defn mask-address
  "Omit address middle characters."
  [address]
  (let [start (apply str (take 5 address))
        end (apply str (take-last 3 address))]
    (str start "..." end)))

(reg-sub
 :wallet/connected?
 (fn [db]
   (boolean (get-in db [:wallet :address]))))

(reg-sub
 :wallet/address
 (fn [db]
   (get-in db [:wallet :address])))

(defn request-fx [{on-success :on-success
                   method :method
                   provider :provider}]
  (-> (.request provider (clj->js {:method method}))
      (.then #(dispatch (conj on-success (js->clj % :keywordize-keys true))))))

(reg-fx
 :wallet/request
 request-fx)

(defn accounts-received-handler [cofx [_ accounts]]
  {:db (-> (:db cofx)
           (assoc-in [:wallet :address] (first accounts)))})

(reg-event-fx
 :wallet/accounts-received
 accounts-received-handler)
