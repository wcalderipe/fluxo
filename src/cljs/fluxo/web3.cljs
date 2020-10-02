(ns fluxo.web3
  (:require ["web3" :as Web3]))

(defn ethereum
  "Get the ethereum propery from js/window."
  []
  (-> js/window .-ethereum))

(defn given-provider []
  (-> Web3 .-givenProvider))

(defn make-web3 [provider]
  (Web3. provider))

(defn contract-class [web3]
  (-> web3 .-eth .-Contract))

(defn set-contract-provider! [web3 provider]
  (.setProvider (contract-class web3) provider))

(defn request-accounts
  "Send a request to get the user's accounts using the ethereum object."
  [ethereum on-success & on-failure]
  (-> (.request ethereum (clj->js {:method "eth_requestAccounts"}))
      (.then #(on-success (js->clj %)))
      (.catch (fn [err]
                (when on-failure
                  #(on-failure (js->clj err)))))))

(comment
  (def web3 (make-web3 (given-provider)))

  (request-accounts (ethereum) #(prn %)))
