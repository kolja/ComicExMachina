(ns ui.page
  (:require [rum.core :as rum]
            [ui.panel :refer [panel]]
            [tools.devtools :refer [log]]
            [tools.helpers :refer [for-indexed]]
            [oops.core :refer [oget ocall]]))

(defonce active-panel (atom nil))

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

(defn mouse-down [prefs page panels cells e]
  (let [[x y] (offset e)
        [cellx celly] (prefs :cell-dimensions)
        cell [(quot x cellx) (quot y celly)]]
    (if (cells cell) 
      (reset! active-panel (cells cell))
      (new-panel-with-cell page panels cells cell))))

(defn mouse-move [prefs page panels cells e]
    (let [[x y] (offset e)
        [cellx celly] (prefs :cell-dimensions)
        cell [(quot x cellx) (quot y celly)]]
      (when @active-panel 
        (if (cells cell) ;; cell exists
          (when-not (= (cells cell) @active-panel)
            (migrate-cell page panels cells cell))
          (new-panel-with-cell page panels cells cell))))) ;; new *panel* on mousemove? fix!

(defn mouse-up [e]
  (reset! active-panel nil))

(rum/defcs page 
  < rum/reactive 
    (rum/local nil ::current-panel)
  [state preferences page]
  (let [prefs (rum/react preferences)
        {:keys [width height current-page]} prefs
        pg (rum/react page)
        panels (pg :panels)
        cells (pg :cells)]
     [:.page {:key (random-uuid)  
              :style {:margin "40px"
                      :padding "20px"
                      :width width 
                      :height (+ 40 height)}}

      [:svg {
             :key "page-svg"
             :width width
             :height height 
             :view-box [0 0 width height]
             :xmlns "http://www.w3.org/2000/svg"
             :on-mouse-down (partial mouse-down prefs page panels cells)
             :on-mouse-move (partial mouse-move prefs page panels cells)
             :on-mouse-up mouse-up
             }
       (for-indexed [p panels] (rum/with-key (panel prefs p) (str "panel" (first p))))]
      ])
)
