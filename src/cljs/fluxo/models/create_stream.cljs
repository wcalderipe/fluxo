(ns fluxo.models.create-stream
  (:require [re-frame.core :as rf]))

(rf/reg-event-db
 ::add-recipient
 (fn [db [_ recipient-addr]]
   (assoc-in db [:create-stream :recipient-addr] recipient-addr)))

(rf/reg-event-db
 ::add-amount
 (fn [db [_ amount]]
   (assoc-in db [:create-stream :amount] amount)))

(rf/reg-event-db
 ::add-token
 (fn [db [_ token]]
   (assoc-in db [:create-stream :token] token)))

(rf/reg-event-db
 ::add-duration
 (fn [db [_ duration]]
   (assoc-in db [:create-stream :duration] duration)))

(rf/reg-sub
 ::recipient-addr
 (fn [db]
   (get-in db [:create-stream :recipient-addr])))

(rf/reg-sub
 ::amount
 (fn [db]
   (get-in db [:create-stream :amount])))

(rf/reg-sub
 ::token
 (fn [db]
   (get-in db [:create-stream :token])))

(rf/reg-sub
 ::duration
 (fn [db]
   (get-in db [:create-stream :duration])))
