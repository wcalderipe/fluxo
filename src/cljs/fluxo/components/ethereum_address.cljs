(ns fluxo.components.ethereum-address
  (:require [fluxo.wallet :refer [mask-address]]))

(defn address [addr]
  [:a.eth-address {:href   (str "https://ropsten.etherscan.io/address/" addr)
                   :target "_blank"}
   (mask-address addr)])
