(ns ui.drawing-area
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [ui.tools :refer [clicked-cell offset]]
            [clojure.string :refer [join]]
            [oops.core :refer [oget ocall]]
            [tools.devtools :refer [log]]))

(enable-console-print!)

(defn mouse-down [state pg-id e]
  (let [appstate (r/cursor state [:appstate])
        page     (r/cursor state [:pages pg-id])
        scale    (get @appstate :scale)
        cells    (@page :cells)
        cell     (clicked-cell state e)
        panel-id (cells cell) ;; TODO: make sure the cell is really within the panel-polygon
        panel    (and panel-id (r/cursor state [:pages pg-id :panels panel-id]))
        ]
    (when panel-id
      (do
        (swap! appstate assoc :active? true)
        (swap! appstate assoc :active-panel panel-id)
        (if (empty? (@panel :strokes))
          (swap! panel assoc :strokes [{:verts [(offset scale e)]}])
          (swap! panel update-in [:strokes] conj {:verts [(offset scale e)]}))))))

(defn mouse-move [state pg-id e]
  (let [appstate (r/cursor state [:appstate])
        scale    (get @appstate :scale)
        panel-id (get @appstate :active-panel)
        panel    (and panel-id (r/cursor state [:pages pg-id :panels panel-id]))
        ]
    (when panel-id 
      (let [
            last-stroke (last (@panel :strokes))
            new-stroke  (update-in last-stroke [:verts] conj (offset scale e))]
        (swap! panel update-in [:strokes] 
               (fn [s] (conj (vec (butlast s)) 
                             new-stroke))
               )) 
      ))
  )

(defn mouse-up [state pg-id e]
  (let [appstate (r/cursor state [:appstate])]
    (swap! appstate assoc :active? false)
    (swap! appstate assoc :active-panel nil))
  )


