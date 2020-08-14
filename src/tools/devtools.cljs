(ns tools.devtools
  (:require [oops.core :refer [ocall oapply]]))

(defn log [& s]
  (oapply js/console :log s))
