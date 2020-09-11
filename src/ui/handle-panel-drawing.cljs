
(ns ui.handle-panel-drawing
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
            [ui.tools :refer [clicked-cell]]
            [oops.core :refer [oget ocall]]
            [tools.devtools :refer [log]]))

(defn add-cell [state page-num panel-id cell]
  (let [page (r/cursor state [:pages page-num])]
    (swap! page update-in [:panels panel-id :cells] conj cell)
    (swap! page update-in [:cells] assoc cell panel-id)
    (ui.panel/walk! state page-num panel-id)))

(defn migrate-cell [state page-num panel-id cell]
  (let [page (r/cursor state [:pages page-num])
        cells (get @page :cells)]
    (swap! page update-in [:panels (cells cell) :cells] (partial remove #{cell}))
    (swap! page update-in [:panels panel-id :cells] conj cell)
    (swap! page update-in [:cells] assoc cell panel-id)
    (ui.panel/walk! state page-num (cells cell))
    (ui.panel/walk! state page-num panel-id)))

(defn mouse-down [state page-num e]
    (let [appstate                (r/cursor state [:appstate])
          page                    (r/cursor state [:pages page-num])
          {:keys [panels cells]}  @page
          cell                    (clicked-cell state e)
          new-panel?              (->> (cells cell) boolean not);; the cell that was clicked on doesn't belong to an existing panel
          panel-id                (or (cells cell) (count panels))]

      (swap! appstate assoc :active-panel panel-id)
      (when new-panel? (add-cell state page-num panel-id cell))
      ))

(defn mouse-move [state page-num e]
  (when (get-in @state [:appstate :active-panel])
    (let [
          appstate      (r/cursor state [:appstate])
          page          (r/cursor state [:pages page-num])
          scale         (get @appstate :scale)
          panel-id      (get @appstate :active-panel)
          panels        (@page :panels)
          cells         (@page :cells)
          cell          (clicked-cell state e)]

      (if (cells cell) ;; cell belongs to existsting panel
        (when-not (= (cells cell) panel-id)
          (migrate-cell state page-num panel-id cell))
        (add-cell state page-num panel-id cell)))))

(defn mouse-up [state page-num e]
  (let [appstate (r/cursor state [:appstate])]
    (swap! appstate assoc :active? false 
           :active-panel nil)))

