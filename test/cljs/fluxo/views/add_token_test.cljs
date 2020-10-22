(ns fluxo.views.add-token-test
  (:require [cljsjs.react]
            [devcards.core :refer-macros [defcard-rg]]
            [fluxo.views.add-token :as view]))

(defcard-rg add-token-component
  (let [model {:recipient-addr "0xfoo111bar"
               :token-symbol   "BAR"
               :amount         "200"}]
    [:div
     [:h2 [:i "Initial state"]]
     [view/add-token-component]]))
