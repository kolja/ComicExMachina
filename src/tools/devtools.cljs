(ns tools.devtools
  (:require [oops.core :refer [ocall oapply]]))

(defn log [& s]
  (oapply js/console :log (if (empty? (rest s)) s (rest s)))
  (first s))
