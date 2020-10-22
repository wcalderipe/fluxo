(ns fluxo.routes
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as rf]))

(def routes ["/" {""             :home
                  "recipient"    {"" :create-stream/recipient}
                  "amount"       {"" :create-stream/amount}
                  "duration"     {"" :create-stream/duration}
                  "confirmation" {"" :create-stream/confirmation}
                  "stream"       {"" :stream/details}
                  "token/new"    {"" :token/new}}])

(defn- dispatch-route [matched-route]
  (rf/dispatch [:routes/redirect-to (:handler matched-route)]))

(defn- parse-url [url]
  (bidi/match-route routes url))

(def history (pushy/pushy dispatch-route parse-url))

(defn start! []
  (pushy/start! history))

(defn url-for [route]
  (bidi/path-for routes route))

(defn set-token! [token]
  (pushy/set-token! history token))

(rf/reg-fx
 ::set-url
 (fn [url]
   (set-token! url)))

(rf/reg-event-fx
 :routes/redirect-to
 (fn [cofx [_ panel]]
   {:db (assoc-in (:db cofx) [:routes :active] panel)
    ::set-url (url-for panel)}))

(rf/reg-sub
 :routes/active
 (fn [db]
   (get-in db [:routes :active])))
