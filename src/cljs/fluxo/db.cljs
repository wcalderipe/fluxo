(ns fluxo.db
  (:require [re-frame.core :refer [reg-event-db reg-sub]]))

(def ^:const ropsten-testdai {:name    "TestDAI"
                              :symbol  "DAI"
                              :address "0x2d69ad895797c880abce92437788047ba0eb7ff6"})

(def default-db {:routes {:active :home}

                 :wallet {:address nil
                          :assets [ropsten-testdai]}})

(defn- with-stream
  "Utility function to initialize the database with a valid stream for development
  purpose."
  [db]
  (assoc db :stream {:id              "140"
                     :deposit-amount  "499999999999999986000"
                     :rate-per-second "27777777777777777"
                     :sender-addr     "0xfOO1111111111111111111111111111111111bAr"
                     :recipient-addr  "0xbAR2222222222222222222222222222222222BaZ"
                     :start-time      1602827222
                     :stop-time       1602845222
                     :token           {:name    "TestDAI"
                                       :symbol  "DAI"
                                       :address "0x2d69ad895797c880abce92437788047ba0eb7ff6"}}))

(reg-event-db
 :db/initialize
 (fn [_ _] default-db))

;; NOTE: this subscription is for development purposes.
(reg-sub
 :db/state
 (fn [db] db))
