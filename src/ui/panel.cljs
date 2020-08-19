(ns ui.panel
  (:require [rum.core :as rum]
            [clojure.string :refer [join]]
            [tools.devtools :refer [log]]
            [oops.core :refer [oget ocall]]))

(defn random-color [] (str "rgb(" 
                           (->> (repeatedly 3 #(Math.floor (+ 10 (* 80 (Math.random)))))
                                (map #(str % "%"))
                                (join ",")) 
                           ")"))

(def colors (into [] (repeatedly 100 random-color)))

(rum/defcs panel 
  < (rum/local (random-color) ::color)
  [state prefs [i pan]]
  (let [[cell-width cell-height] (prefs :cell-dimensions)
        cells (pan :cells)
        rc (colors i)]
  [:g {:color rc
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

