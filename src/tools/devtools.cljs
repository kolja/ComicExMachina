(ns tools.devtools
  (:require [oops.core :refer [ocall oapply]]))

(defn log [s]
  (ocall js/console :log (clj->js s))
  s)
