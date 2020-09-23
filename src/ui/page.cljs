(ns ui.page
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
            [ui.panel :refer [panel]]
            [ui.tools :refer [lpad]]
            [ui.handle-panel-drawing :as pd]
            [ui.drawing-area :as dr]
            [tools.devtools :refer [log]]
            [tools.helpers :refer [for-indexed]]
            [oops.core :refer [oget oset! ocall]]))


(defonce ipc (-> (js/require "electron") 
                 (oget :ipcRenderer)))

(defn grid [state pg-id]
  (fn [state pg-id]
    (let [{:keys [preferences appstate pages]}  @state
          {:keys [grid-width grid-height] [cw ch] :cell-dimensions m :margin} preferences
          page-num                        (get-in pages [pg-id :num])
          s                               (appstate :scale)
          grid-gray                       "#ddd"
          is-left?                        (zero? (mod page-num 2))
          [m0 m1 m2 m3]                   (if is-left?
                                            [(m 0) (m 1) (m 2) (m 3)]
                                            [(m 0) (m 3) (m 2) (m 1)])]
      
      [:g {:fill grid-gray}
        [:rect {
                :x (* cw m3) :y (* ch m0) :width (* cw (- grid-width m1 m3)) :height (* ch (- grid-height m1 m3))
                }]
        (when (not is-left?) 
          [:line 
           {:stroke-width 2
            :stroke "#ccc"
            :stroke-dasharray "7,7" :x1 1 :y1 1 :x2 1 :y2 (* ch grid-height)}])]
      )))

(defn svg-path [verts cw ch offset scale]
  (if (empty? verts) ""
    (str "M " (join "L " (for [{:keys [x y] [nx ny] :normal} verts] 
                           (str (- (* cw x) (* offset nx)) " " 
                                (- (* ch y) (* offset ny))))) " Z")))

(defn clip-paths [pg-id panels cw ch paths]
  [:defs
   (for [[pn-id path] paths :let [[[x y] [_ _]] (get-in panels [pn-id :bounding-box])
                                   p2 (partial lpad "0" 2)]]
     [:g {:key (str "clip-" pg-id "-" pn-id)}
      [:clipPath {;; clippath has to counteract the transform of it's content :-(
                  :transform  (str "translate(" (* -1 cw x) "," (* -1 ch y) ")")
                  :id (str "clipimg-" (p2 pg-id) "-" (p2 pn-id))}
       [:path {:d path}]]

      [:clipPath {:id (str "clipsvg-" (p2 pg-id) "-" (p2 pn-id))}
       [:path {:d path}]]]
     )])

(defn panel-outline [paths pn-id]
  [:path {:key (str "outline" pn-id)
          :d (paths pn-id) 
          :stroke "black"
          :stroke-width 2
          :fill "none"}])

(defn page [state pg-id]
  (fn [state pg-id]
    (let [
          appstate               (r/cursor state [:appstate])
          prefs                  (get @state :preferences)
          export-path            (get prefs :export-path)
          panels                 (get-in @state [:pages pg-id :panels])
          {:keys [width height gutter-width] [cw ch] :cell-dimensions} prefs
          offset                 (/ gutter-width 2)
          {:keys [scale tool]}   @appstate
          lpad2                  (partial lpad "0" 2)

          all-paths              (into {} (map (fn [[k v]] [k (svg-path (get v :verts) cw ch offset scale)]) panels))
          handler                {:panels  {:down pd/mouse-down :move pd/mouse-move :up pd/mouse-up}
                                  :drawing {:down dr/mouse-down :move dr/mouse-move :up dr/mouse-up}}]
      [:div.page
       
       [:svg {
              :key (random-uuid)
              :width (* scale width)
              :height (* scale height) 
              :view-box [0 0 width height]
              :xmlns "http://www.w3.org/2000/svg"
              :on-mouse-down (partial (get-in handler [tool :down]) state pg-id)
              :on-mouse-move (partial (get-in handler [tool :move]) state pg-id)
              :on-mouse-up   (partial (get-in handler [tool :up  ]) state pg-id)
              :style {:background-color "#eee"}
              }


        [clip-paths pg-id panels cw ch all-paths]
        [grid state pg-id]
        
        (for [pn-id (keys panels)]
          [:g {:key (str "panel" pn-id)}
            [panel state pg-id pn-id]
            [panel-outline all-paths pn-id]
            ]
          )]
        

       ;(for [pn-id (keys panels)]
       ;  )
       ]
      )))

(defn blank [state]
  (fn [state]
    (let [scale                  (get-in @state [:appstate :scale])
          {:keys [width height]} (get @state :preferences)]
    [:div.blank {
           :style {:width (* scale width)
                   :height (* scale height)
                   :background-color "#333"} }])))

(defn spread-page [state l r]
  (fn [state l r]
    (let [current-page (get-in @state [:appstate :current-page])]
      (if (<= (- l 2) current-page (+ 2 r))
        [:div.spread 
         {:style {:margin 15
                  :display "flex"}}
         (if (nil? l) [blank state] [page state l])
         (if (nil? r) [blank state] [page state r])]
        [:div.spread
         [blank state] [blank state]]))))

