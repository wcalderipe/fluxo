(ns fluxo.views.confirmation-step-test
  (:require [cljsjs.react]
            [devcards.core :as dc :refer-macros [defcard]]
            [re-frame.core :as rf]
            [fluxo.views.confirmation-step :as view]))

#_(defcard
    (dc/reagent
     [:div
      [:h3 [:i "Initial state"]]
      [view/recipient-step-component {:recipient-addr nil}]

      [:h3 [:i "Recipient input filled"]]
      [view/recipient-step-component {:recipient-addr "0xfoo111bar"}]]))
