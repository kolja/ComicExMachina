(ns ui.overview
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [tools.devtools :refer [log]]))

(enable-console-print!)

(defn overview [prefs pages current-page]
    [:div.overview
      [:ol
        (doall (for [n (range (count @pages))]
          [:li {:key n
                :class [(when (= n @current-page) "current")]
                :on-click #(when-not (zero? n) (reset! current-page n))} 
           (if (zero? n) "" (str n))]
        ))]

      [:button
       {:on-click #(swap! pages conj {:panels [] :cells {}})}
       (str "add page")]
    ])

