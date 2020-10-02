(ns fluxo.db
  (:require [re-frame.core :refer [reg-event-db reg-sub]]))

(def default-db {:active-panel :home-panel
                 :wallet {:address nil}})

(reg-event-db
 :db/initialize
 (fn [_ _] default-db))

;; NOTE: this subscription is for development purposes.
(reg-sub
 :db/state
 (fn [db] db))
