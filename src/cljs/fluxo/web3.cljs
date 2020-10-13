(ns fluxo.web3
  (:require ["web3" :as Web3]
            [re-frame.core :as rf]))

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

(rf/reg-cofx
 :web3/ethereum
 (fn [cofx]
   (assoc cofx :web3/ethereum (ethereum!))))

(rf/reg-cofx
 :web3/provider
 (fn [cofx]
   (assoc cofx :web3/provider (given-provider))))

(rf/reg-event-fx
 ::save-ethereum-presence
 [(rf/inject-cofx :web3/ethereum)]
 (fn [cofx _]
   (let [present? (boolean (:web3/ethereum cofx))]
     {:db (assoc-in (:db cofx) [:web3 :ethereum-present?] present?)})))

(comment
  ;; Request user's accounts --------------------------------------------------
  (request-accounts (ethereum!) #(prn %)))
