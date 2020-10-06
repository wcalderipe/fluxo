(ns fluxo.cards
  (:require [cljsjs.react]
            [cljsjs.react.dom]
            [devcards.core :refer [start-devcard-ui!]]
            [devcards.core :as dc :refer [defcard]]))

(defn ^:export init
  "Start the devcards UI."
  []
  ;; Add a special class to the body to signal we're in devcards mode.
  ;; We want to mostly use the same styles as the app, but might need to make
  ;; some exceptions.
  (js/document.body.classList.add "using-devcards")
  ;; Start the devcards UI.
  (start-devcard-ui!))
