(ns ui.page
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
            [ui.panel]
            [tools.devtools :refer [log]]
            [tools.helpers :refer [for-indexed]]
            [oops.core :refer [oget oset! ocall]]))

(defn offset [e]
  (let [bound (ocall e "currentTarget.getBoundingClientRect")]
    [(- (oget e :pageX) (oget bound :left))
     (- (oget e :pageY) (oget bound :top))]))

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

(defn mouse-down [tool state page-num e]
  (when (= tool :panels)
    (let [
          appstate  (r/cursor state [:appstate])
          page      (r/cursor state [:pages page-num])
          prefs     (get @state :preferences)
          scale     (get @appstate :scale)
          [x y]     (map #(/ % scale) (offset e))
          panels    (@page :panels)
          cells     (@page :cells)
          [cellx celly] (prefs :cell-dimensions)
          cell      [(quot x cellx) (quot y celly)]
          new-panel? (not (boolean (cells cell))) ;; the cell that was clicked on doesn't belong to an existing panel TODO: threading
          panel-id  (or (cells cell) (count panels))]
      (swap! appstate assoc :active-panel panel-id)
      (when new-panel? (add-cell state page-num panel-id cell))
      )))

(defn mouse-move [tool state page-num e]
  (when (and (= tool :panels) (get-in @state [:appstate :active-panel]))
    (let [
          appstate      (r/cursor state [:appstate])
          page          (r/cursor state [:pages page-num])
          scale         (get @appstate :scale)
          [x y]         (map #(/ % scale) (offset e))
          panel-id     (get @appstate :active-panel)
          panels        (@page :panels)
          cells         (@page :cells)
          [cellx celly] (get-in @state [:preferences :cell-dimensions])
          cell          [(quot x cellx) (quot y celly)]]

      (if (cells cell) ;; cell belongs to existsting panel
        (when-not (= (cells cell) panel-id)
          (migrate-cell state page-num panel-id cell))
        (add-cell state page-num panel-id cell)))))

(defn mouse-up [appstate e]
  (swap! appstate assoc :active? false 
                        :active-panel nil))

(defn clip-paths [prefs page-num panels]
  (fn [prefs page-num panels]

    (let [panels  @panels
          [cw ch] (prefs :cell-dimensions)
          offset  (/ (prefs :gutter-width) 2)]

      [:defs
       (for [panel-id (range (count panels))]
         [:clipPath {:key (str "clippath-" page-num "-" panel-id)
                     :id (str "clip-" page-num "-" panel-id)}
          [:polygon {:key (str "polyclip-" panel-id)
                     :points (join " " (for [{:keys [x y] [nx ny] :normal} (get-in panels [panel-id :verts])] 
                                         (str (- (* cw x) (* offset nx)) "," 
                                              (- (* ch y) (* offset ny)))))
                     :stroke "yellow"
                     :stroke-width 6
                     }]])])))

(defn draw-grid [ctx state page-num]
    (let [{:keys [preferences appstate]}  @state

          {:keys [grid-width grid-height] [cw ch] :cell-dimensions m :margin} 
                                          preferences

          s                               (get-in @state [:appstate :scale])
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
           (oset! ctx :lineWidth 3)
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
               {:keys [scale tool]}   @appstate]
           (when @dom-node
             (draw dom-node state page-num))
           [:div.with-canvas 
            {:on-mouse-down (partial mouse-down tool state page-num)
             :on-mouse-move (partial mouse-move tool state page-num)
             :on-mouse-up (partial mouse-up appstate)
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

