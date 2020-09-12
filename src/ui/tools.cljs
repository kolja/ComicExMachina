
(ns ui.tools
  (:require [oops.core :refer [oget ocall]]
            [tools.devtools :refer [log]]))

(defn in-bounding-box? 
  "is the cell within the bounding box?"
  [[[bx1 by1] [bx2 by2]] [x y]]  
  (and (<= bx1 x bx2) 
       (<= by1 y by2)))

(defn offset [scale e]
  (let [bound (ocall e "currentTarget.getBoundingClientRect")]
    [(/ (- (oget e :pageX) (oget bound :left)) scale)
     (/ (- (oget e :pageY) (oget bound :top)) scale)]))

(defn clicked-cell 
  "over which cell did this event occur?"
  [state e]
  (let [state         @state
        [cellx celly] (get-in state [:preferences :cell-dimensions])
        scale         (get-in state [:appstate :scale])
        [x y]         (offset scale e)]

    [(quot x cellx) (quot y celly)]))

; TODO: all of the following angle/winding-number stuff can be done with javascript + offline canvas.
;let offscreen = new OffscreenCanvas(256, 256);
;let ctx = offscreen.getContext('2d');
;let p = new Path2D;

;p.rect(10, 20, 150, 100);
;isinpath = ctx.isPointInPath(p, 140, 30)

(defn angle [cell [v1 v2]]
  (let [[x y] cell
        {v1x :x v1y :y} v1
        {v2x :x v2y :y} v2
        ax (- v1x x)
        ay (- v1y y)
        bx (- v2x x)
        by (- v2y y)
        direction (if (pos? (- (* by ax) (* ay bx))) 1 -1)]
    (* direction (.acos js/Math
                        (/ (+ (* ax bx) 
                              (* ay by)) 
                           (.sqrt js/Math 
                                  (* (+ (* ax ax) (* ay ay)) 
                                     (+ (* bx bx) (* by by)))))))))

(defn winding-number 
  "calculates the winding number and returns if it's (roughly) smaller than tau (aka 2 x PI)"
  [cell verts]
  (->> (partition 2 1 (conj verts (first verts)))
       (map (fn [vs] (angle cell vs)))
       (apply +)))

(defn inside? [cell verts]
  (< 3 (winding-number cell verts)))
