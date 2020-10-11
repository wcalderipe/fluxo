(ns fluxo.views.duration-step-test
  (:require [cljsjs.react]
            [devcards.core :as dc :refer-macros [defcard]]
            [fluxo.views.duration-step :as view]))

(defcard
  (let [model {:recipient-addr "0xfoo111bar"
               :token-symbol   "BAR"
               :amount         "200"}]
    (dc/reagent
     [:div
      [:h2 [:i "Initial state"]]
      [view/duration-step-component model]

      [:h2 [:i "Form filled"]]
      [view/duration-step-component (merge model {:duration 2.3})]])))
