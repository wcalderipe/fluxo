(ns fluxo.etherscan
  (:require [ajax.core :refer [json-request-format json-response-format]]
            [re-frame.core :refer [reg-event-fx]]))

(def ^:const network-base-url {:ropsten "https://api-ropsten.etherscan.io/api"})

(defn get-contract-abi-handler [_ [_ {address    :address
                                      on-success :on-success}]]
  {:http-xhrio {:method          :get
                :uri             (:ropsten network-base-url)
                :params          {:module  "contract"
                                  :action  "getabi"
                                  :format  "raw"
                                  :address address}
                :timeout         5000
                :format          (json-request-format)
                :response-format (json-response-format {:keywords? true})
                :on-success      on-success}})

(reg-event-fx
 :etherscan/get-contract-abi
 get-contract-abi-handler)
