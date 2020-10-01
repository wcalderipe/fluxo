(ns fluxo.wallet
  (:require [re-frame.core :refer [reg-sub]]))

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
