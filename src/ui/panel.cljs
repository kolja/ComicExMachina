(ns ui.panel
  (:require [rum.core :as rum]
            [tools.devtools :refer [log]]
            [oops.core :refer [oget ocall]]))

(defn random-color [] (vec (repeatedly 3 #(Math.floor (+ 10 (* 80 (Math.random)))))))

(rum/defc panel [prefs pan]
  (let [[cell-width cell-height] (prefs :cell-dimensions)
        color1 (str )
        cells (pan :cells)
        rc (random-color)]
  [:g {:color (str "rgb(" (rc 0) "%," (rc 1) "%," (rc 2) "%)")
       :key (random-uuid)}
   (for [cell cells]
     [:rect {
             :key (random-uuid)
             :x (* cell-width (cell 0))
             :y (* cell-height (cell 1))
             :width cell-width
             :height cell-height
             :fill "currentcolor"
             :stroke-width 2
             :stroke "black"
             }])]))

