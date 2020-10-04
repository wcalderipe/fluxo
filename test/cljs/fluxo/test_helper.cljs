(ns fluxo.test-helper
  (:require [cljs.test :refer-macros [is are deftest testing use-fixtures]]
            [reagent.core :as reagent :refer [atom]]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]))

(def client? (not (nil? (try (.-document js/window)
                             (catch js/Object e nil)))))

(def rflush reagent/flush)

(defn add-test-div [name]
  (let [doc     js/document
        body    (.-body js/document)
        div     (.createElement doc "div")]
    (.appendChild body div)
    div))

(defn with-mounted-component [comp f]
  (when client?
    (let [div (add-test-div "_test_mounted_component")]
      (let [comp (rdom/render comp div #(f comp div))]
        (rdom/unmount-component-at-node div)
        (rflush)
        (.removeChild (.-body js/document) div)))))

(defn found-in [re div]
  (let [res (.-innerHTML div)]
    (if (re-find re res)
      true
      (do (println "not found: " res)
          false))))

(defn fixture-re-frame []
  (let [restore-re-frame (atom nil)]
    {:before #(reset! restore-re-frame (rf/make-restore-fn))
     :after  #(@restore-re-frame)}))
