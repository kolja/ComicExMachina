(ns ui.template
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [tools.devtools :refer [log]]))

(enable-console-print!)

(defn mouse-down [foo])

(defn template [foo bar baz]
  (fn [foo bar baz]
    (let []
      [:circle {:key (random-uuid)
                :width width
                :height height 
                :on-mouse-down (partial mouse-down foo)}])))
