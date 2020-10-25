(ns fluxo.components-test
  (:require [cljsjs.react]
            [devcards.core :refer-macros [defcard-rg]]
            [fluxo.components.ethereum-address :refer [address]]))

(defcard-rg ethereum-address
  [:div
   [address "0x2d69ad895797c880abce92437788047ba0eb7ff6"]])
