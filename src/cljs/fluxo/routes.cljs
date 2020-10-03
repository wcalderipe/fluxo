(ns fluxo.routes
  (:require [bidi.bidi :refer [match-route path-for]]
            [pushy.core :refer [pushy start!]]
            [re-frame.core :refer [dispatch reg-event-db reg-sub]]))

(def routes ["/" {"" :home
                  "recipient" {"" :create-stream/recipient}}])

(defn- dispatch-route [matched-route]
  (dispatch [:routes/redirect-to (:handler matched-route)]))

(defn- parse-url [url]
  (match-route routes url))

(defn app-routes []
  (start! (pushy dispatch-route parse-url)))

(defn url-for [route]
  (path-for routes route))

(defn redirect-to-handler
  "Handler to set the value of :active-panel in the db."
  [db [_ panel]]
  (assoc-in db [:routes :active] panel))

(reg-event-db
 :routes/redirect-to
 redirect-to-handler)

(defn get-active-route-handler
  "Get the active panel from the db."
  [db]
  (get-in db [:routes :active]))

(reg-sub
 :routes/active
 get-active-route-handler)
