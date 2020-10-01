(ns fluxo.db
  (:require [re-frame.core :refer [reg-sub]]))

(def default-db {:active-panel :onboarding-panel
                 :wallet {:address nil}})

;; NOTE: this subscription is for development purposes.
(reg-sub
 :db
 (fn [db] db))
