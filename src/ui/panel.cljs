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

(defn rotate [[cx cy] n] 
  (nth (map (fn [[x y]] [(+ cx x) (+ cy y)]) 
            [[0 0][1 0][1 1][0 1]]) 
       (mod n 4)))

(defn make-verts [verts cell from to] 
  (let [angle (mod (+ 2 (- from to)) 4)
        [cx cy] (rotate cell (+ from 3))
        [c1x c1y] (rotate cell from)
        [c3x c3y] (rotate cell (+ from 2))]
    (condp = angle
      0 (conj verts {:x cx :y cy :normal []} {:x c1x :y c1y :normal []})
      1 (conj verts {:x cx :y cy :normal []})
      2 verts
      3 (conj verts {:x c3x :y c3y :normal []}))))

(defn walk-the-line [prefs cells cursor]
  (let [cells (into #{} cells) ;; TODO: make sure cells is a set to begin with, then delete
        {:keys [cell to from verts n]} cursor
        neighbours (cell-neighbours prefs cell)
        next-cell (nth neighbours to)
        mod4 (fn [f] (fn [n] (-> n f (mod 4))))]
    (cond 
      (empty? cells) verts
      (every? #(or (nil? %) (not (contains? cells %))) neighbours)
        (let [[x y] cell]
          [{:x x :y y :normal []} ; -1 -1
           {:x (inc x) :y y :normal []} ; 1 -1
           {:x (inc x) :y (inc y) :normal []} ; 1 1
           {:x x :y (inc y) :normal []}]) ; -1 1
      (and (>= (count verts) 4) 
           (or (= (first verts) (last verts)) (= (first verts) (first (take-last 2 verts)))))
        (butlast verts)
      (contains? cells next-cell)
        (let [new-cursor (-> cursor 
                             (assoc :cell next-cell) 
                             (assoc :from to)
                             (update :n inc)
                             (update :to (mod4 dec)))]
          (if (nil? from)
            (walk-the-line prefs cells new-cursor)
            (walk-the-line prefs cells 
                           (-> new-cursor
                               (assoc :verts (make-verts verts cell from to)))))) 
      :else
        (walk-the-line prefs cells (-> cursor 
                                       (update :to (mod4 inc))
                                       (update :n inc)))
      )))

(defn panel 
  [prefs [i panel]]
  (fn [prefs [i panel]] 
    (let [[cell-width cell-height] (prefs :cell-dimensions)
          cells (panel :cells)
          rc (colors i)
          [x y :as cell] (upper-left-corner cells)
          cursor {:cell cell :to 2 :from nil
                  :verts []}
          verts (walk-the-line prefs cells cursor)]
      [:g {:color rc}
       [:polygon {:key "poly"
                  :points (join " " (for [{:keys [x y]} verts] (str (* cell-width x) "," (* cell-height y))))
                  :stroke "black"
                  :fill "currentcolor"}]]
      )))

