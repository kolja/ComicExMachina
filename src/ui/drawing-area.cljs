(ns ui.drawing-area
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
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

(defn drawing-area [prefs appstate panel panel-id]
  (fn [prefs appstate panel panel-id]
    (let [
          [cw ch]                   (prefs :cell-dimensions)
          verts                     (@panel :verts)
          offset                    (/ (prefs :gutter-width) 2)
          [[bb1x bb1y] [bb2x bb2y]] (@panel :bounding-box)
          x                         (get (first verts) :x)
          y                         (get (first verts) :y)
          strokes                   (@panel :strokes)
          ]

      [:g 
       {:style {:clip-path (str "url(\u0023clip-" panel-id ")")}}
       [:polygon {:key "mouse-area"
                  :points (join " " (for [{:keys [x y] [nx ny] :normal} verts] 
                                      (str (* cw x)  ", " (* ch y))))

                  :on-mouse-down (partial mouse-down appstate panel)
                  :on-mouse-move (partial mouse-move appstate panel)
                  :on-mouse-up   (partial mouse-up   appstate panel)
                  :fill "transparent"}]

        (for [s (range (count strokes))]
          [:polyline {:key (str "stroke-" s)
                      :fill "none"
                      :stroke "#4a8ac7"
                      :transform (str "translate(" (join " " [(* cw bb1x) (* ch bb1y)]) ")")
                      :stroke-width 2
                      :points (join " " (flatten (get-in strokes [s :verts])))}])]
       )))
