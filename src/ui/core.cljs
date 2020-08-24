(ns ui.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [reagent.ratom :as ra]
            [tools.devtools :refer [log]]
            [ui.overview :refer [overview]]
            [ui.page :refer [page]]
            [oops.core :refer [ocall]]))

(enable-console-print!)

(defonce state (r/atom {
                        :id 0
                        :current-page 1
                        :preferences {
                                      :gutter-width 6
                                      :grid-width 12
                                      :grid-height 12
                                      :width 210
                                      :height 297
                                      }
                        :pages [{}          ;; page "zero" stays blank
                                {:panels [{ :cells [] }] 
                                 :cells {}} ;; empty doc at least contains one page
                                ]}))

(defn root [state]

  (let [preferences (@state :preferences)
        {:keys [width height grid-width grid-height]} preferences]
    (swap! state assoc-in [:preferences :cell-dimensions]
           [(/ width grid-width)
            (/ height grid-height)])

    (fn [state]
      (let [preferences   (r/cursor state [:preferences])
            prefs         @preferences
            all-pages     (r/cursor state [:pages])
            page-num      (r/cursor state [:current-page])
            current-page  (r/cursor state [:pages @page-num])]
        [:div.root {:key "root"}
         [overview preferences all-pages page-num]
         ^{:key "page"} [page preferences current-page]])
      )
    ))

(rd/render [root state] (ocall js/document :getElementById "app"))

