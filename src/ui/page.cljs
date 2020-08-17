(ns ui.page
  (:require [rum.core :as rum]
            [tools.devtools :refer [log]]
            [oops.core :refer [oget ocall]]))

(defn offset [e]
  (let [bound (ocall e "currentTarget.getBoundingClientRect")]
    [(- (oget e :pageX) (oget bound :left))
     (- (oget e :pageY) (oget bound :top))]))

(defn new-panel-with-cell [page panels cells cell]
  (let [panel (count panels)]
    (swap! page update-in [:panels] conj {:cells [cell]})
    (swap! page update-in [:cells] assoc cell panel)))

(defn mouse-down [prefs page panels cells e]
  (let [[x y] (offset e)
        [cellx celly] (prefs :cell-dimensions)
        cell [(quot x cellx) (quot y celly)]]
    ;; {:panels [{:cells [[0 0][1 1]]}]
    ;;  :cells {[0 0] 0 [1 1] 0}}
    (when-not (cells cell) (new-panel-with-cell page panels cells cell))))

(defn mouse-move [e]
    (log (str (offset e))))

(rum/defc panel [prefs pan rc]
  (let [[cell-width cell-height] (prefs :cell-dimensions)
        color1 (str )]
  [:g {:color (str "rgb(" (rc 0) "%," (rc 1) "%," (rc 2) "%)")}
   (for [cell (pan :cells)]
   [:rect {
           :x (* cell-width (cell 0))
           :y (* cell-height (cell 1))
           :width cell-width
           :height cell-height
           :fill "currentcolor"
           :stroke-width 2
           :stroke "black"
           }])]))

(defn random-color [] (vec (repeatedly 3 #(Math.floor (+ 10 (* 80 (Math.random)))))))

(rum/defc page < rum/reactive [preferences page]
  (let [prefs (rum/react preferences)
        {:keys [width height current-page]} prefs
        pg (rum/react page)
        panels (pg :panels)
        cells (pg :cells)]
     [:.page {:style {:margin "40px"
                      :padding "20px"
                      :width width 
                      :height (+ 40 height)}}

      [:svg {
             :width width
             :height height 
             :view-box [0 0 width height]
             :xmlns "http://www.w3.org/2000/svg"
             :on-mouse-down (partial mouse-down prefs page (or panels []) (or cells {}))
             ; :on-mouse-move mouse-move
             }
       (for [pan panels] (panel prefs pan (random-color)))
       ]
      ])
)
