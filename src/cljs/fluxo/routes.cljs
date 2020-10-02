(ns fluxo.routes
  (:require [bidi.bidi :refer [match-route path-for]]
            [pushy.core :refer [pushy start!]]
            [re-frame.core :refer [dispatch reg-event-db reg-sub]]))

(def routes ["/" {"" :home}])

(defn- dispatch-route [matched-route]
  (let [panel-name (keyword (str (name (:handler matched-route)) "-panel"))]
    (dispatch [:routes/set-active-panel panel-name])))

(defn- parse-url [url]
  (match-route routes url))

(defn app-routes []
  (start! (pushy dispatch-route parse-url)))

(def url-for (partial path-for routes))

(defn set-active-panel-handler
  "Handler to set the value of :active-panel in the db."
  [db [_ panel]]
  (assoc db :active-panel panel))

(reg-event-db
 :routes/set-active-panel
 set-active-panel-handler)

(defn get-active-panel
  "Get the active panel from the db."
  [db]
  (:active-panel db))

(reg-sub
 :routes/active-panel
 get-active-panel)
