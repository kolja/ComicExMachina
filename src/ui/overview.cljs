(ns ui.overview
  (:require [rum.core :as rum]
            [tools.devtools :refer [log]]))

(enable-console-print!)

(rum/defc overview < rum/reactive [prefs pages current-page]
    [:div.overview
      [:ol
        (for [n (range (count (rum/react pages)))]
          [:li {:key n
                :class [(when (= n (rum/react current-page)) "current")]
                :on-click #(when-not (zero? n) (reset! current-page n))} 
           (if (zero? n) "" (str n))]
        )]

      [:button
       {:on-click #(swap! pages conj {:panels [] :cells {}})}
       (str "add page")]
    ])

