(ns ui.page
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
            [ui.panel :refer [panel]]
            [tools.devtools :refer [log]]
            [tools.helpers :refer [for-indexed]]
            [oops.core :refer [oget ocall]]))

(defonce active-panel (r/atom nil))

(defn offset [e]
  (let [bound (ocall e "currentTarget.getBoundingClientRect")]
    [(- (oget e :pageX) (oget bound :left))
     (- (oget e :pageY) (oget bound :top))]))

(defn new-panel-with-cell [page panels cells cell]
  (let [panel (or @active-panel (count panels))]
    (reset! active-panel panel)
    (swap! page update-in [:panels panel :cells] conj cell)
    (swap! page update-in [:cells] assoc cell panel)))

(defn migrate-cell [page panels cells cell]
  (swap! page update-in [:panels (cells cell) :cells] (partial remove #{cell}))
  (swap! page update-in [:panels @active-panel :cells] conj cell)
  (swap! page update-in [:cells] assoc cell @active-panel))

(defn mouse-down [prefs appstate page e]
  (when (= (@appstate :tool) :panels)
    (let [[x y] (offset e)
          panels (@page :panels)
          cells (@page :cells)
          [cellx celly] (prefs :cell-dimensions)
          cell [(quot x cellx) (quot y celly)]]
      (if (cells cell) 
        (reset! active-panel (cells cell))
        (new-panel-with-cell page panels cells cell)))))

(defn mouse-move [prefs appstate page e]
  (when (= (@appstate :tool) :panels)
    (let [[x y] (offset e)
        panels (@page :panels)
        cells (@page :cells)
        [cellx celly] (prefs :cell-dimensions)
        cell [(quot x cellx) (quot y celly)]]
      (when @active-panel 
        (if (cells cell) ;; cell exists
          (when-not (= (cells cell) @active-panel)
            (migrate-cell page panels cells cell))
          (new-panel-with-cell page panels cells cell)))))) ;; new *panel* on mousemove? fix!

(defn mouse-up [appstate e] ;; TODO: use active-panel from the appstate
  (swap! appstate assoc :drawing? false)
  (reset! active-panel nil))

(defn clip-paths [prefs panels]
  (fn [prefs panels]

    (let [panels  @panels
          [cw ch] (prefs :cell-dimensions)
          offset  (/ (prefs :gutter-width) 2)]

      [:defs
       (for [panel-id (range (count panels))]
         [:clipPath {:key (str "clippath-" panel-id)
                     :id (str "clip-" panel-id)}
          [:polygon {:key (str "polyclip-" panel-id)
                     :points (join " " (for [{:keys [x y] [nx ny] :normal} (get-in panels [panel-id :verts])] 
                                         (str (- (* cw x) (* offset nx)) "," 
                                              (- (* ch y) (* offset ny)))))
                     :stroke "yellow"
                     :stroke-width 6
                     }]])]
      )))

(defn page [state]
  (fn [state]
    (let [page-num      (get-in @state [:appstate :current-page])     
          page          (r/cursor state [:pages page-num])
          preferences   (r/cursor state [:preferences])
          appstate      (r/cursor state [:appstate])
          prefs @preferences
          {:keys [width height]} prefs
          panels  (r/cursor page [:panels])]
      [:div.page {:key (random-uuid)  
               :style {:margin "40px"
                       :padding "20px"
                       :width width 
                       :height (+ 40 height)}}
       [:svg {
              :key (random-uuid)
              :width width
              :height height 
              :view-box [0 0 width height]
              :xmlns "http://www.w3.org/2000/svg"
              :on-mouse-down (partial mouse-down prefs appstate page)
              :on-mouse-move (partial mouse-move prefs appstate page)
              :on-mouse-up (partial mouse-up appstate)
              }
        [clip-paths prefs panels]
        (for [i (range (count @panels))]
          ^{:key (str "panel-" i)} [panel prefs appstate page i]
          )]])))



