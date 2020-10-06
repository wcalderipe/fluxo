(ns fluxo.cards
  (:require [cljsjs.react.dom]
            [cljsjs.react]
            [devcards.core :refer [start-devcard-ui!]]
            [fluxo.create-stream]
            [fluxo.onboarding]))

(defn ^:export init
  "Start the devcards UI."
  []
  ;; Add a special class to the body to signal we're in devcards mode.
  ;; We want to mostly use the same styles as the app, but might need to make
  ;; some exceptions.
  (js/document.body.classList.add "using-devcards")
  ;; Start the devcards UI.
  (start-devcard-ui!))
