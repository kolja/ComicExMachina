(ns ui.page
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
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

(defn mouse-down [prefs page e]
  (let [[x y] (offset e)
        panels (@page :panels)
        cells (@page :cells)
        [cellx celly] (prefs :cell-dimensions)
        cell [(quot x cellx) (quot y celly)]]
    (if (cells cell) 
      (reset! active-panel (cells cell))
      (new-panel-with-cell page panels cells cell))))

(defn mouse-move [prefs page e]
    (let [[x y] (offset e)
        panels (@page :panels)
        cells (@page :cells)
        [cellx celly] (prefs :cell-dimensions)
        cell [(quot x cellx) (quot y celly)]]
      (when @active-panel 
        (if (cells cell) ;; cell exists
          (when-not (= (cells cell) @active-panel)
            (migrate-cell page panels cells cell))
          (new-panel-with-cell page panels cells cell))))) ;; new *panel* on mousemove? fix!

(defn mouse-up [e]
  (reset! active-panel nil))

(defn page [preferences page]
  (fn [preferences page]
    (let [prefs @preferences
          {:keys [width height current-page]} prefs
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
              :on-mouse-down (partial mouse-down prefs page)
              :on-mouse-move (partial mouse-move prefs page)
              :on-mouse-up mouse-up
              }
        (for [i (range (count @panels))]
          ^{:key (str "panel-" i)} [panel prefs (r/cursor page [:panels i]) i]
          )

        #_(for-indexed [p panels] ^{:key (str "panel-" (first p))} [panel prefs p])

        ]
       ])))
