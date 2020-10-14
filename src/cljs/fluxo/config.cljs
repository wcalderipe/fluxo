(ns fluxo.config)

(def debug?
  ^boolean goog.DEBUG)

(def sablier-time-buffer
  "Sets the time buffer used to safely create a Sablier stream ahead of the
  blocktime."
  (* 10 60))
