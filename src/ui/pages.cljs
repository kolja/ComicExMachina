(ns ui.pages
  (:require [rum.core :as rum]
            [tools.devtools :refer [log]]))

(enable-console-print!)

(defn init-cells [max-x max-y]
  (for [x (range max-x) y (range max-y)] [x y]))

(defn set-current-page [e]
  (log e))

(rum/defc pages < rum/reactive [prefs pages current-page]
    [:div.pages
      [:ol
        (for [n (range (count (rum/react pages)))]
          [:li {:key n
                :class [(when (= n (rum/react current-page)) "current")]
                :on-click #(reset! current-page n)} 
           (str n)]
        )]

      [:button
       {:on-click #(swap! pages conj {:panels []})}
       (str "add page")]
    ])

