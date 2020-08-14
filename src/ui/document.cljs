(ns ui.document
  (:require [rum.core :as rum]
            [tools.devtools :refer [log]]
            [oops.core :refer [oget]]))

(defn offset [e]
  (let [x (- (oget e :pageX) (oget e :currentTarget :offsetLeft))
        y (- (oget e :pageY) (oget e :currentTarget :offsetTop))]
    [x y]))

(defn mouse-down [e]
    (log (str (offset e))))

(defn mouse-move [e]
    (log (str (offset e))))

(rum/defc document [prefs page]
  [:.document
   [:.page.active
    {:on-mouse-down mouse-down
     :on-mouse-move mouse-move
     }]]
)
