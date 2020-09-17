(ns ui.overview
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [tools.devtools :refer [log]]))

(enable-console-print!)

(defn overview [state]

  (let [pages         (r/cursor state [:pages])
        current-page  (r/cursor state [:appstate :current-page])
        pgs           @pages
        next-index    (inc (apply max (keys pgs)))
        next-page-num (inc (count pgs))]

      [:div.overview
       [:ol
               
        (doall (for [[pg-id pg] (concat [[]] (sort-by #(get-in (val %) [:num]) pgs))]
                 [:li {:key (or pg-id (random-uuid))
                       :class [(when (= pg-id @current-page) "current")]
                       :on-click #(when-not (nil? pg-id) (reset! current-page pg-id))} 
                  (if (nil? pg-id) "" (str (get pg :num)))]
                 ))]

       [:button
        {:on-click #(swap! pages assoc next-index {:num next-page-num :panels {} :cells {}})}
        (str "add page")]
       ]))


