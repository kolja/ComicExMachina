(ns ui.drawing-area
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
            [oops.core :refer [oget ocall]]
            [tools.devtools :refer [log]]))

(enable-console-print!)

(defn offset [scale e]
  (let [bound (ocall e "currentTarget.getBoundingClientRect")
        bbox  (ocall e "currentTarget.getBBox")
        s     (/ 1 scale)
        pageX (oget e :pageX)
        pageY (oget e :pageY)
        left  (oget bound :left)
        top   (oget bound :top)
        x     (oget bbox :x)
        y     (oget bbox :y)]
    [(-> pageX (- left) (* s) (+ x))
     (-> pageY (- top ) (* s) (+ y))]))

(defn mouse-down [appstate panel e]
  (when (= (@appstate :tool) :drawing)
    (do
      (swap! appstate assoc :active? true)
      (if (empty? (@panel :strokes))
        (swap! panel assoc :strokes [{:verts [(offset (get @appstate :scale) e)]}])
        (swap! panel update-in [:strokes] conj {:verts [(offset (get @appstate :scale) e)]})))))

(defn mouse-move [appstate panel e]
  (when (@appstate :active?)
    (let [last-stroke (last (@panel :strokes))
          new-stroke  (update-in last-stroke [:verts] conj (offset (get @appstate :scale) e))]
      (swap! panel update-in [:strokes] 
             (fn [s] (conj (vec (butlast s)) 
                           new-stroke))
             )) 
    )
  )

(defn mouse-up [appstate panel]
  (when (= (@appstate :tool) :drawing)
    (swap! appstate assoc :active? false))
  )

(defn drawing-area [state page-num panel-id]
  (fn [state page-num panel-id]
    (let [
          panel                     (r/cursor state [:pages page-num :panels panel-id])
          prefs                     (get @state :preferences)
          appstate                  (r/cursor state [:appstate])
          scale                     (get @appstate :scale)
          [cw ch]                   (prefs :cell-dimensions)
          verts                     (@panel :verts)
          offset                    (/ (prefs :gutter-width) 2)
          [[bb1x bb1y] [bb2x bb2y]] (@panel :bounding-box)
          x                         (get (first verts) :x)
          y                         (get (first verts) :y)
          strokes                   (@panel :strokes)
          ]

      [:g 

       [:polygon {:key "mouse-area"
                  :points (join " " (for [{:keys [x y] [nx ny] :normal} verts] 
                                      (str (* cw x)  ", " (* ch y))))

                  :on-mouse-down (partial mouse-down appstate panel)
                  :on-mouse-move (partial mouse-move appstate panel)
                  :on-mouse-up   (partial mouse-up   appstate panel)
                  :fill "transparent"}]

        ;; TODO: perhaps move the actual line-drawing to panel and this file is for drawing logic only?
        (for [s (range (count strokes))]
          [:polyline {:key (str "stroke-" s)
                      :style {:pointer-events "none"
                              :clip-path (str "url(\u0023clip-" page-num "-" panel-id ")")
                              }
                      :fill "none"
                      :stroke "#4a8ac7"
                      :stroke-width 2
                      :points (join " " (flatten (get-in strokes [s :verts])))}])
        ]
       )))
