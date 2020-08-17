(ns ui.document
  (:require [rum.core :as rum]
            [tools.devtools :refer [log]]
            [oops.core :refer [oget ocall]]))

(defn offset [e]
  (let [bound (ocall e "currentTarget.getBoundingClientRect")]
    [(- (oget e :pageX) (oget bound :left))
     (- (oget e :pageY) (oget bound :top))]))

(defn mouse-down [prefs panels e]
  (let [[x y] (offset e)]
    (log (str x "-" y "-" prefs))))

(defn mouse-move [e]
    (log (str (offset e))))

(rum/defc panel [p]
  [:g {:color "black"
       :on-mouse-down #(log p)}
   [:rect {
           :x 0
           :y 0
           :width 24
           :height 24
           :fill "none"
           :stroke-width 2
           :stroke "currentcolor"
           }]])

(rum/defc document < rum/reactive [prefs page]
  (let [prefs (rum/react prefs)
        panels (rum/react page)]
    [:.document
     [:.page
      [:svg {
             :width (prefs :width)
             :height (prefs :height)
             :view-box [0 0 (prefs :width) (prefs :height)]
             :xmlns "http://www.w3.org/2000/svg"
             :on-mouse-down (partial mouse-down prefs panels)
             ; :on-mouse-move mouse-move
             }
       (panel)
       ;(let [panels ((rum/react page) :panels)]
       ;    (for [p panels] 
       ;      (panel p)))
       ]
      ]])
)
