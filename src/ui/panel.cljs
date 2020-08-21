(ns ui.panel
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
            [tools.devtools :refer [log]]
            [tools.helpers :refer [for-indexed]]
            [oops.core :refer [oget ocall]]))

(defn random-color [] (str "rgb(" 
                           (->> (repeatedly 3 #(Math.floor (+ 10 (* 80 (Math.random)))))
                                (map #(str % "%"))
                                (join ",")) 
                           ")"))

(def colors (into [] (repeatedly 100 random-color)))

(defn panel 
  [prefs [i panel]]
  (fn [prefs [i panel]] 
    (let [[cell-width cell-height] (prefs :cell-dimensions)
          cells (panel :cells)
          rc (colors i)]
      [:g {:color rc}
       [:polygon {:key "poly"
                  :points (join " " (for [[x y] cells] (str (* cell-width x) "," (* cell-height y))))
                  :stroke "black"
                  :fill "currentcolor"}]]
      )))

