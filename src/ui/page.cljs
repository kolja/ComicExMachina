(ns ui.page
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
            [ui.panel]
            [ui.tools :refer [lpad]]
            [ui.handle-panel-drawing :as pd]
            [ui.drawing-area :as dr]
            [tools.devtools :refer [log]]
            [tools.helpers :refer [for-indexed]]
            [oops.core :refer [oget oset! ocall]]))


(defonce ipc (-> (js/require "electron") 
                 (oget :ipcRenderer)))

(defn draw-grid [ctx state pg-id]
    (let [{:keys [preferences appstate pages]}  @state
          {:keys [grid-width grid-height] [cw ch] :cell-dimensions m :margin} preferences
          page-num                        (get-in pages [pg-id :num])
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

(defn strokes 
  ;; TODO: state will be derefed many times this way; perhaps only alow one arity with panel-id as optional arg.
  ([state pg-id panel-id]
   (let [path (js/Path2D.)
         strokes (get-in @state [:pages pg-id :panels panel-id :strokes])
         scale (get-in @state [:appstate :scale])]

     (doseq [{:keys [verts]} strokes]
       (ocall path :addPath
              (js/Path2D. (str "M" (join "L" (for [[x y] verts] (str (* scale x) " " (* scale y))))))))
     path)
   )
  ([state pg-id]
   (let [panels (get-in @state [:pages pg-id :panels])
                  path (js/Path2D.)]

     (doseq [p-id (range (count panels))]
       (ocall path :addPath (strokes state pg-id p-id)))
     path
     )))

(defn svg-path [verts cw ch offset scale]
  (if (empty? verts) ""
    (str "M " (join "L " (for [{:keys [x y] [nx ny] :normal} verts] 
                           (str (- (* cw x scale) (* offset nx scale)) " " 
                                (- (* ch y scale) (* offset ny scale))))) " Z")))

(defn draw-image [ctx path pg-id panel-id]
  (let [image (js/Image.)
        pgn   (lpad pg-id "0" 2)
        pid   (lpad panel-id "0" 2)]
    (oset! image :src (str "file://" path "page" pgn "/panel" pid ".png"))
    (oset! image :onerror (fn [e] (ocall js/console :log (str "couldn't load page" pgn "/panel" pid " :" (clj->js e)))))
    (oset! image :onload (fn [] (ocall ctx :drawImage image 0 0 )))))

(defn draw [dom-node state pg-id]

  (let [s            @state
        canvas       (oget @dom-node :firstChild)
        ctx          (ocall canvas :getContext "2d")
        scale        (get-in s [:appstate :scale])
        panels       (get-in s [:pages pg-id :panels])
        prefs        (get-in s [:preferences])
        [cw ch]      (prefs :cell-dimensions)
        offset       (/ (prefs :gutter-width) 2)
        export-path  (get prefs :export-path)
        all-paths    (into {} (map (fn [[k v]] [k (svg-path (get v :verts) cw ch offset scale)]) panels))
        ]

    ;(ocall ipc :invoke "server-hello", (str "page-" pg-id)) 

    (draw-grid ctx state pg-id)

    (oset! ctx :lineWidth (ocall js/Math :ceil (* 2 scale)))
    (oset! ctx :fillStyle "white")

    (ocall ctx :fill (js/Path2D. (join " " (vals all-paths))))

    (ocall ctx :save)

    (oset! ctx :lineWidth (ocall js/Math :ceil scale))
    (oset! ctx :strokeStyle "#4a8ac7")

    (doseq [panel-id (keys panels)]
      (let [panel (get all-paths panel-id)]
      
        (ocall ctx :save)
        (ocall ctx :clip (js/Path2D. panel))
        (ocall ctx :stroke (strokes state pg-id panel-id))
        (ocall ctx :restore)

      ))

    (ocall ctx :restore)

    (ocall ctx :stroke (js/Path2D. (join " " (vals all-paths))))
 ))

(defn page [state pg-id]

  (let [dom-node (r/atom nil)]

    (r/create-class
      {:component-did-update
       (fn [this] (draw dom-node state pg-id))

       :component-did-mount
       (fn [this] 
         (reset! dom-node (rd/dom-node this))
         (draw dom-node state pg-id))

       :reagent-render
       (fn []

         (let [appstate               (r/cursor state [:appstate])
               {:keys [width height]} (get @state :preferences)
               {:keys [scale tool]}   @appstate
               handler                {:panels  {:down pd/mouse-down :move pd/mouse-move :up pd/mouse-up}
                                       :drawing {:down dr/mouse-down :move dr/mouse-move :up dr/mouse-up}}
               ]
           (when @dom-node
             (draw dom-node state pg-id))
           [:div.with-canvas 
            {:on-mouse-down (partial (get-in handler [tool :down]) state pg-id)
             :on-mouse-move (partial (get-in handler [tool :move]) state pg-id)
             :on-mouse-up   (partial (get-in handler [tool :up  ]) state pg-id)
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
         (if (nil? l) [blank state] [page state l])
         (if (nil? r) [blank state] [page state r])]
        [:div.spread
         [blank state] [blank state]]))))

