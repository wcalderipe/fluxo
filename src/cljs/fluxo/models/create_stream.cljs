(ns fluxo.models.create-stream
  (:require [re-frame.core :as rf]))

(rf/reg-event-db
 ::add-recipient
 (fn [db [_ recipient-addr]]
   (assoc-in db [:create-stream :recipient-addr] recipient-addr)))

(rf/reg-sub
 ::recipient-addr
 (fn [db]
   (get-in db [:create-stream :recipient-addr])))
