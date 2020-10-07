(ns fluxo.db
  (:require [re-frame.core :refer [reg-event-db reg-sub]]
            [fluxo.wallet :refer [->Asset]]))

(def ^:const ropsten-testdai (->Asset "TestDAI"
                                      "DAI"
                                      "0x2d69ad895797c880abce92437788047ba0eb7ff6"))

(def default-db {:routes {:active :home}

                 :wallet {:address nil
                          :assets [ropsten-testdai]}})

(reg-event-db
 :db/initialize
 (fn [_ _] default-db))

;; NOTE: this subscription is for development purposes.
(reg-sub
 :db/state
 (fn [db] db))
