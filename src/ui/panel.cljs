(ns ui.panel
  (:require [rum.core :as rum]
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

(def before-render
    {:before-render (fn [state]
          (let [[prefs [i panel]] (:rum/args state)]
            (log (str "-" i "->" panel)))
          state
    )})

(rum/defcs panel 
  < (rum/local (random-color) ::color)
  < before-render
  [state prefs [i panel]]
  (let [[cell-width cell-height] (prefs :cell-dimensions)
        cells (panel :cells)
        rc (colors i)]
    [:g {:color rc}
     [:polygon {:points (join " " (for [[x y] cells] (str (* cell-width x) "," (* cell-height y))))
                :stroke "black"
                :fill "currentcolor"}]
     ;(for-indexed [[cell-idx [x y]] cells]
     ;             [:rect {:key (str "cell-" i "-" cell-idx)
     ;                     :x (* cell-width x)
     ;                     :y (* cell-height y)
     ;                     :width cell-width
     ;                     :height cell-height
     ;                     :fill "currentcolor"
     ;                     :stroke-width 2
     ;                     :stroke "black"
     ;                     }]
     ;             )
     ]))

