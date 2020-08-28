(ns ui.drawing-area
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [oops.core :refer [oget ocall]]
            [tools.devtools :refer [log]]))

(enable-console-print!)

(defn offset [e] ; TODO: This function was copy-pasted from page. Perhaps keep mouse-event related stuff in one place?
  (let [bound (ocall e "currentTarget.getBoundingClientRect")]
    [(- (oget e :pageX) (oget bound :left))
     (- (oget e :pageY) (oget bound :top))]))

(defn mouse-down [appstate panel e]
  (when (= (@appstate :tool) :drawing)
    (do
      (log "start drawing")
      (swap! appstate assoc :drawing? true)
      (if (empty? (@panel :strokes))
        (swap! panel assoc :strokes [{:verts [(offset e)]}])
        (swap! panel update-in [:strokes] conj {:verts [(offset e)]})))))

(defn mouse-move [appstate panel e]
  (when (@appstate :drawing?)
    (let [last-stroke (last (@panel :strokes))
          new-stroke  (update-in last-stroke [:verts] conj (offset e))]
      (swap! panel update-in [:strokes] 
             (fn [s] (conj (vec (butlast s)) 
                           new-stroke))
             )) 
    )
  )

(defn mouse-up [appstate panel]
  (when (= (@appstate :tool) :drawing)
    (swap! appstate assoc :drawing? false))
  )

(defn drawing-area [prefs appstate panel]
  (fn [prefs mode panel]
    (let [
          [cw ch]                   (prefs :cell-dimensions)
          verts                     (@panel :verts)
          [[bb1x bb1y] [bb2x bb2y]] (@panel :bounding-box)
          x                         (get (first verts) :x)
          y                         (get (first verts) :y)
          strokes                   (@panel :strokes)
          ]
      [:g {
           :transform (str "translate(" (.join clojure.string " " [(* cw bb1x) (* ch bb1y)]) ")")
           }
       (for [s (range (count strokes))]
         [:polyline {:key (str "stroke-" s)
                     :fill "none"
                     :stroke "black"
                     :stroke-width 2
                     :points (.join clojure.string " " (flatten (get-in strokes [s :verts])))}])

       [:rect {; :x (* cw bb1x)
               ; :y (* ch bb1y)
               :width (* cw (- bb2x bb1x))
               :height (* ch (- bb2y bb1y))
               :fill "transparent"
               :on-mouse-down (partial mouse-down appstate panel)
               :on-mouse-move (partial mouse-move appstate panel)
               :on-mouse-up   (partial mouse-up   appstate panel)
               }]])))
