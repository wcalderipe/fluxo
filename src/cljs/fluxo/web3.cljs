(ns fluxo.web3
  (:require-macros [fluxo.resources :refer [inline]])
  (:require ["web3" :as Web3]
            ["web3-utils" :as web3-utils]
            [re-frame.core :refer [dispatch reg-cofx reg-fx]]))

(defn ethereum!
  "Get the ethereum propery from js/window."
  []
  (.-ethereum js/window))

(defn given-provider []
  (.. Web3 -givenProvider))

(defn make-web3 [provider]
  (Web3. provider))

(defn contract-class [web3]
  (.. web3 -eth -Contract))

(defn make-contract [web3 abi address]
  (new (.. web3 -eth -Contract) (clj->js abi) address))

(defn set-contract-provider! [web3 provider]
  (.setProvider (contract-class web3) provider))

(defn request-accounts
  "Send a request to get the user's accounts using the ethereum object."
  [ethereum on-success]
  (-> (.request ethereum (clj->js {:method "eth_requestAccounts"}))
      (.then #(on-success (js->clj %)))))

(defn fetch-accounts
  [ethereum on-success]
  (-> (.request ethereum (clj->js {:method "eth_accounts"}))
      (.then #(on-success (js->clj %)))))

(reg-cofx
 :web3/ethereum
 (fn [cofx]
   (assoc cofx :web3/ethereum (ethereum!))))

(reg-cofx
 :web3/provider
 (fn [cofx]
   (assoc cofx :web3/provider (given-provider))))

(comment
  ;; Request user's accounts --------------------------------------------------
  (request-accounts (ethereum!) #(prn %)))
