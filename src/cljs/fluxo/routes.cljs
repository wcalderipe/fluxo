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

(reg-event-db :routes/set-active-panel
              (fn [db [_ panel]]
                (assoc db :active-panel panel)))

(reg-sub :routes/active-panel
 (fn [db]
   (:active-panel db)))
