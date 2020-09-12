(ns ui.panel
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [clojure.string :refer [join]]
            [tools.devtools :refer [log]]
            [ui.tools :refer [inside?]]
            [tools.helpers :refer [for-indexed]]
            [oops.core :refer [oget oset! ocall]]))

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
      acc
      (recur prefs cells 
             (cond
               (empty? cells) (assoc acc :done? true)
               (every? #(or (nil? %) (not (contains? cells %))) neighbours)
               (-> acc 
                   (assoc :verts (single-cell-verts cell))
                   (assoc :visited-cells #{cell})
                   (assoc :done? true))
               (and (>= (count verts) 4) 
                    (or (= (first verts) (last verts)) (= (first verts) (first (take-last 2 verts)))))
               (-> acc
                   (update :verts butlast)
                   (assoc :done? true))
               (contains? cells next-cell)
               (let [new-acc (-> acc 
                                 (assoc :cell next-cell) 
                                 (update :visited-cells conj next-cell)
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


(defn bounding-box 
  "returns a bounding box [upper-left lower-right] that encompasses all cells"
  [verts]
  (let [xs (sort-by :x verts)
        ys (sort-by :y verts)]
    [[(get (first xs) :x) (get (first ys) :y)]
     [(get (last  xs) :x) (get (last  ys) :y)]]))

(defn cells-in-bb [[[x1 y1] [x2 y2]]]
  (for [x (range x1 x2) 
        y (range y1 y2)]
    [x y]))

(defn cleanup 
  "reset this panel's boundry box
  list of all cells in this boundry box, minus cells visited by 'walk-the-line' 
  for each remaining cell: 
    sum of angles between it and all the verts (aka: calculate winding-number)
    zero? discard. otherwise add it to visited cells.
  return visited cells (so the panel's cells can be updated)"
  [panel visited-cells verts]
  (let [bb             (bounding-box verts)
        cells          (cells-in-bb bb)       
        cells-to-check (remove visited-cells cells)]
    (concat
      visited-cells
      (for [cell cells-to-check :when (inside? [(+ (first cell) 0.5) (+ (last cell) 0.5)] verts)] cell)
      )))

(defn remove-disconnected [cells panel-id]
    (fn [c]
      (let [c1 (apply dissoc c (->> c
                                    (filter #(= panel-id (second %))) 
                                    (map first))) ] ; remove all cells that belong to panel 'i'
        (merge c1 (zipmap cells (repeat panel-id))))))

(defn walk! [state page-num panel-id]
  (let [prefs                    (get @state :preferences)
        [cell-width cell-height] (prefs :cell-dimensions)
        page                     (r/cursor state [:pages page-num])
        panel                    (r/cursor page [:panels panel-id])
        cells                    (@panel :cells) ;; will be rebound after 'walk-the-line'
        offset                   (/ (prefs :gutter-width) 2)
        [x y :as cell]           (upper-left-corner cells)
        acc                      {:cell cell 
                                  :visited-cells #{}
                                  :to 2 
                                  :from nil
                                  :verts []}
        {:keys [verts visited-cells]}   (walk-the-line prefs cells acc)
        cells                    (if (empty? verts) []
                                   (cleanup panel visited-cells verts))]

      (when-not (= (@panel :verts) verts) ; avoid unnecessary updates
        (swap! panel assoc :cells cells 
                           :verts verts
                           :bounding-box (bounding-box verts))
        (swap! page update :cells (remove-disconnected cells panel-id)))))

