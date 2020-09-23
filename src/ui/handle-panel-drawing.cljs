
(ns ui.handle-panel-drawing
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
            [ui.tools :refer [clicked-cell]]
            [oops.core :refer [oget ocall]]
            [tools.devtools :refer [log]]))

(defn add-cell [state pg-id panel-id cell]
  (let [page   (r/cursor state [:pages pg-id])
        panels (get @page :panels)]
    (if (contains? panels panel-id) 
      (swap! page update-in [:panels panel-id :cells] conj cell)
      (swap! page update-in [:panels] assoc panel-id {:cells [cell]}))
    (swap! page update-in [:cells] assoc cell panel-id)
    (ui.panel/walk! state pg-id panel-id)))

(defn migrate-cell [state pg-id panel-id cell]
  (let [page (r/cursor state [:pages pg-id])
        cells (get @page :cells)]
    (swap! page update-in [:panels (cells cell) :cells] (partial remove #{cell}))
    (swap! page update-in [:panels panel-id :cells] conj cell)
    (swap! page update-in [:cells] assoc cell panel-id)
    (ui.panel/walk! state pg-id (cells cell))
    (ui.panel/walk! state pg-id panel-id)))

(defn mouse-down [state pg-id e]
  (let [appstate      (r/cursor state [:appstate])
        page          (r/cursor state [:pages pg-id])

        cells         (get @page :cells)
        panels        (get @page :panels)

        cell          (clicked-cell state e)
        new-panel?    (->> (cells cell) boolean not);; the cell that was clicked on doesn't belong to an existing panel
        max-panel-id  (if (empty? panels) 0 (inc (apply max (keys panels))))
        panel-id      (or (cells cell) max-panel-id)]

    (swap! appstate assoc :active-panel panel-id)
    (when new-panel? (add-cell state pg-id panel-id cell))
    ))

(defn mouse-move [state pg-id e]
  (when (get-in @state [:appstate :active-panel])
    (let [
          appstate      (r/cursor state [:appstate])
          page          (r/cursor state [:pages pg-id])
          scale         (get @appstate :scale)
          panel-id      (get @appstate :active-panel)
          panels        (@page :panels)
          cells         (@page :cells)
          cell          (clicked-cell state e)]

      (if (cells cell) ;; cell belongs to existsting panel
        (when-not (= (cells cell) panel-id)
          (migrate-cell state pg-id panel-id cell))
        (add-cell state pg-id panel-id cell)))))

(defn mouse-up [state pg-id e]
  (let [appstate (r/cursor state [:appstate])]
    (swap! appstate assoc :active? false 
           :active-panel nil)))

