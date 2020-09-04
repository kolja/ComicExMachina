(ns ui.page
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
            [ui.panel :refer [panel]]
            [tools.devtools :refer [log]]
            [tools.helpers :refer [for-indexed]]
            [oops.core :refer [oget ocall]]))

(defn offset [e]
  (let [bound (ocall e "currentTarget.getBoundingClientRect")]
    [(- (oget e :pageX) (oget bound :left))
     (- (oget e :pageY) (oget bound :top))]))

(defn new-panel-with-cell [appstate page panels cells cell]
  (let [active-panel  (get @appstate :active-panel)
        panel (or active-panel (count panels))]
    (swap! appstate assoc :active-panel panel)
    (swap! page update-in [:panels panel :cells] conj cell)
    (swap! page update-in [:cells] assoc cell panel)))

(defn migrate-cell [appstate page panels cells cell]
  (let [active-panel (get @appstate :active-panel)]
    (swap! page update-in [:panels (cells cell) :cells] (partial remove #{cell}))
    (swap! page update-in [:panels active-panel :cells] conj cell)
    (swap! page update-in [:cells] assoc cell active-panel)))

(defn mouse-down [preferences appstate page e]
  (when (= (@appstate :tool) :panels)
    (let [[x y] (offset e)
          prefs @preferences
          panels (@page :panels)
          cells (@page :cells)
          [cellx celly] (prefs :cell-dimensions)
          cell [(quot x cellx) (quot y celly)]]
      (if (cells cell) 
        (log (swap! appstate assoc :active-panel (cells cell)))
        (new-panel-with-cell appstate page panels cells cell)))))

(defn mouse-move [preferences appstate page e]
  (when (= (@appstate :tool) :panels)
    (let [[x y]         (offset e)
          prefs         @preferences
          active-panel  (get @appstate :active-panel)
          panels        (@page :panels)
          cells         (@page :cells)
          [cellx celly] (prefs :cell-dimensions)
          cell [(quot x cellx) (quot y celly)]]
      (when active-panel 
        (if (cells cell) ;; cell exists
          (when-not (= (cells cell) active-panel)
            (migrate-cell appstate page panels cells cell))
          (new-panel-with-cell appstate page panels cells cell)))))) ;; new *panel* on mousemove? fix!

(defn mouse-up [appstate e] ;; TODO: use active-panel from the appstate
  (swap! appstate assoc :drawing? false 
                        :active-panel nil))

(defn clip-paths [prefs page-num panels]
  (fn [prefs page-num panels]

    (let [panels  @panels
          [cw ch] (prefs :cell-dimensions)
          offset  (/ (prefs :gutter-width) 2)]

      [:defs
       (for [panel-id (range (count panels))]
         [:clipPath {:key (str "clippath-" page-num "-" panel-id)
                     :id (str "clip-" panel-id)}
          [:polygon {:key (str "polyclip-" panel-id)
                     :points (join " " (for [{:keys [x y] [nx ny] :normal} (get-in panels [panel-id :verts])] 
                                         (str (- (* cw x) (* offset nx)) "," 
                                              (- (* ch y) (* offset ny)))))
                     :stroke "yellow"
                     :stroke-width 6
                     }]])])))

(defn grid [state page-num]
  (fn [state page-num]
    (let [{:keys [preferences appstate]} @state
          {:keys [grid-width grid-height] [cw ch] :cell-dimensions m :margin} 
                                        preferences
          grid-gray                     "#ddd"
          is-left?                      (zero? (mod page-num 2))

          [m0 m1 m2 m3]                 (if is-left? [(m 0) (m 1) (m 2) (m 3)]
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

(defn page [state page-num]
  (fn [state page-num]
    (let [scale                  (get-in @state [:appstate :scale])
          {:keys [width height]} (get @state :preferences)
          page                   (r/cursor state [:pages page-num])
          preferences            (r/cursor state [:preferences])
          appstate               (r/cursor state [:appstate])
          panels                 (r/cursor page [:panels])
          ]
       [:svg {
              :key (random-uuid)
              :width (* scale width)
              :height (* scale height) 
              :view-box [0 0 width height]
              :xmlns "http://www.w3.org/2000/svg"
              :on-mouse-down (partial mouse-down preferences appstate page)
              :on-mouse-move (partial mouse-move preferences appstate page)
              :on-mouse-up (partial mouse-up appstate)
              }
        [clip-paths @preferences page-num panels]
        [grid state page-num]
        (for [panel-id (range (count @panels))]
          ^{:key (str "panel-" panel-id)} [panel state page-num panel-id]
          )])))

(defn blank [state]
  (fn [state]
    (let [{:keys [width height]} (get @state :preferences)]
    [:div.blank {
           :style {:width width
                   :height height
                   :background-color "#333"} }])))


(defn spread-page [state l r]
  (fn [state l r]

     [:div.spread 
      (if (zero? l) [blank state] [page state l])
      (if (zero? r) [blank state] [page state r])]))

