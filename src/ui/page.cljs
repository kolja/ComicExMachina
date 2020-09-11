(ns ui.page
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
            [ui.panel]
            [ui.handle-panel-drawing :as pd]
            [ui.drawing-area :as dr]
            [tools.devtools :refer [log]]
            [tools.helpers :refer [for-indexed]]
            [oops.core :refer [oget oset! ocall]]))

(defn draw-grid [ctx state page-num]
    (let [{:keys [preferences appstate]}  @state
          {:keys [grid-width grid-height] [cw ch] :cell-dimensions m :margin} preferences
          s                               (appstate :scale)
          grid-gray                       "#ddd"
          is-left?                        (zero? (mod page-num 2))
          [m0 m1 m2 m3]                   (if is-left? 
                                            [(m 0) (m 1) (m 2) (m 3)]
                                            [(m 0) (m 3) (m 2) (m 1)])]
      
      (oset! ctx :fillStyle "white")
      (ocall ctx :fillRect 0 0 (* cw grid-width s) (* ch grid-height s))
      (oset! ctx :fillStyle grid-gray)
      (ocall ctx :fillRect (* cw m3 s) (* ch m0 s) (* cw (- grid-width m1 m3) s) (* ch (- grid-height m1 m3) s))
      ))


(defn svg-path [verts cw ch offset scale]
    (str "M " (join "L " (for [{:keys [x y] [nx ny] :normal} verts] 
                             (str (- (* cw x scale) (* offset nx scale)) " " 
                                  (- (* ch y scale) (* offset ny scale))))) " Z"))

(defn draw [dom-node state page-num]

  (let [page                   (r/cursor state [:pages page-num])
        preferences            (r/cursor state [:preferences])
        appstate               (r/cursor state [:appstate])
        panels                 (r/cursor page [:panels])
        canvas  (oget @dom-node :firstChild)
        ctx     (ocall canvas :getContext "2d")
        scale   (get-in @state [:appstate :scale])
        panels  @panels
        prefs   @preferences
        [cw ch] (prefs :cell-dimensions)
        offset  (/ (prefs :gutter-width) 2)]

           (draw-grid ctx state page-num)
           (oset! ctx :lineWidth (ocall js/Math :ceil (* 2 scale)))
           (oset! ctx :fillStyle "white")
           (doseq [panel-id (range (count panels))]
             (let [verts (get-in panels [panel-id :verts])
                   path  (js/Path2D. (svg-path verts cw ch offset scale))]
               (ocall ctx :fill path)
               (ocall ctx :stroke path)
               ))))

(defn page [state page-num]

  (let [dom-node (r/atom nil)]

    (r/create-class
      {:component-did-update
       (fn [this] (draw dom-node state page-num))

       :component-did-mount
       (fn [this] 
         (reset! dom-node (rd/dom-node this))
         (draw dom-node state page-num))

       :reagent-render
       (fn []

         (let [appstate               (r/cursor state [:appstate])
               {:keys [width height]} (get @state :preferences)
               {:keys [scale tool]}   @appstate
               handler                {:panels {:down pd/mouse-down :move pd/mouse-move :up pd/mouse-up}
                                       :draw   {:down dr/mouse-down :move dr/mouse-move :up dr/mouse-up}}
               ]
           (when @dom-node
             (draw dom-node state page-num))
           [:div.with-canvas 
            {:on-mouse-down (partial (get-in handler [tool :down]) state page-num)
             :on-mouse-move (partial (get-in handler [tool :move]) state page-num)
             :on-mouse-up   (partial (get-in handler [tool :up  ]) state page-num)
             :style {:background-color "white"
                     :width (* scale width)
                     :height (* scale height)}}

            [:canvas 
             {:width (* scale width)
              :height (* scale height)}]]))
       }
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
         (if (zero? l) [blank state] [page state l])
         (if (zero? r) [blank state] [page state r])]
        [:div.spread
         [blank state] [blank state]]))))

