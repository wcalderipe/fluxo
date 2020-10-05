(ns fluxo.resources
  (:require [clojure.java.io :as io]))

(defmacro inline [path]
  (slurp (io/resource path) :encoding "utf8"))
