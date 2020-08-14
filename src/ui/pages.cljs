(ns ui.pages
  (:require [rum.core :as rum]))

(enable-console-print!)

(rum/defc pages < rum/reactive [state]
  (let [pages (get-in (rum/react state) [:pages])]
    [:div.pages
      [:ol
        (for [n (range (count pages))]
          [:li {:key n} (str n)]
        )
      ]

      [:button
       {:on-click #(swap! state update-in [:pages] conj {:panels []})}
       (str "add page")]
    ]
  )
)

