(ns ui.panel
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
            [tools.devtools :refer [log]]
            [tools.helpers :refer [for-indexed]]
            [oops.core :refer [oget ocall]]))

(defn random-color [] (str "rgb(" 
                           (->> (repeatedly 3 #(Math.floor (+ 10 (* 80 (Math.random)))))
                                (map #(str % "%"))
                                (join ",")) 
                           ")"))

(def colors (into [] (repeatedly 100 random-color)))

(defn cell-neighbours [{:keys [grid-width grid-height]} [x y]] 
  (map (fn [[x y]] 
         (if (and (<= 0 x grid-width)
                  (<= 0 y grid-height)) [x y] nil))
          [[(dec x) y]
           [x (dec y)]
           [(inc x) y]
           [x (inc y)]]))

(defn upper-left-corner [cells]
  (or (first (sort-by (juxt first second) cells)) []))

(defn bounding-box 
  "returns a bounding box [upper-left lower-right] that encompasses all cells"
  [cells]
  (let [byx (sort-by first cells)
        byy (sort-by second cells)]
    [[(ffirst byx) (second (first byy))]
     [(first (last byx)) (second (last byy))]]))

(defn in-bounding-box? 
  "is the cell within the bounding box?"
  [[[bx1 by1] [bx2 by2]] [x y]]  
  (and (<= bx1 x bx2) 
       (<= by1 y by2)))

(defn rotate [[cx cy] n] 
  (nth (map (fn [[x y]] [(+ cx x) (+ cy y)]) 
            [[0 0][1 0][1 1][0 1]]) 
       (mod n 4)))

(defn mod4 
  ([f n] (mod (f n) 4)) 
  ([n]   (mod n 4)))

(defn make-verts [verts cell from to] 
  (let [angle (mod4 (- from to))
       normals [[-1 -1] [1 -1] [1 1] [-1 1]]
       [cx cy] (rotate cell (+ from 3))
       [c1x c1y] (rotate cell from)
       [c3x c3y] (rotate cell (+ from 2))]
    (condp = angle
      0 verts ;; from == to -> going in a straight line. No vertex added
      1 (conj verts {:x c3x :y c3y :normal (nth normals (mod4 (+ to 3)))})
      2 (conj verts {:x cx :y cy :normal (nth normals (mod4 inc to))} {:x c1x :y c1y :normal (nth normals (mod4 (+ to 2)))})
      3 (conj verts {:x cx :y cy :normal (nth normals (mod4 (+ to 2)))})
      )))

(defn single-cell-verts [[x y]]
    [{:x x :y y :normal [-1 -1]}
     {:x (inc x) :y y :normal [1 -1]}
     {:x (inc x) :y (inc y) :normal [1 1]}
     {:x x :y (inc y) :normal [-1 1]}])

(defn walk-the-line [prefs cells acc]
  (let [cells (into #{} cells) ;; TODO: make sure cells is a set to begin with, then delete
        {:keys [cell to from verts n done?]} acc
        neighbours (cell-neighbours prefs cell)
        next-cell (nth neighbours to)]
    (if done? 
      verts
      (recur prefs cells 
             (cond
               (empty? cells) (assoc acc :done? true)
               (every? #(or (nil? %) (not (contains? cells %))) neighbours)
               (-> acc 
                   (assoc :verts (single-cell-verts cell))
                   (assoc :done? true))
               (and (>= (count verts) 4) 
                    (or (= (first verts) (last verts)) (= (first verts) (first (take-last 2 verts)))))
               (-> acc
                   (update :verts butlast)
                   (assoc :done? true))
               (contains? cells next-cell)
               (let [new-acc (-> acc 
                                    (assoc :cell next-cell) 
                                    (assoc :from to)
                                    (update :n inc)
                                    (update :to (comp mod4 dec)))] 
                 (if (nil? from) 
                   (update new-acc :verts identity) 
                   (assoc new-acc :verts (make-verts verts cell from to)))) ;; TODO: check equal verts right here and set done to true (if equal)
               :else 
               (-> acc 
                   (update :to (comp mod4 inc))
                   (update :n inc)))))))

(defn panel 
  [prefs [i panel]]
  (fn [prefs [i panel]] 
    (let [[cell-width cell-height] (prefs :cell-dimensions)
          cells (panel :cells)
          offset (/ (prefs :gutter-width) 2)
          rc (colors i)
          [x y :as cell] (upper-left-corner cells)
          acc {:cell cell :to 2 :from nil
               :verts []}
          verts (walk-the-line prefs cells acc)]
      [:g {:color rc}
       [:polygon {:key "poly"
                  :points (join " " (for [{:keys [x y] [nx ny] :normal} verts] 
                                      (str (- (* cell-width x) (* offset nx)) "," 
                                           (- (* cell-height y) (* offset ny)))))
                  :stroke "black"
                  :fill "currentcolor"}]
       (for-indexed [[i c] cells] 
                    ^{:key (str "circle-" i)} [:circle {:cx (* cell-width (+ 0.5 (first c))) 
                                                        :cy (* cell-height (+ 0.5 (last c)))
                                                        :r "5"
                                                        :stroke "black"
                                                        :stroke-width "2"
                                                        :fill "currentcolor" }])
       ]
      )))

