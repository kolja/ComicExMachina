(ns ui.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [reagent.ratom :as ra]
            [tools.devtools :refer [log]]
            [ui.overview :refer [overview]]
            [ui.page :refer [page]]
            [ui.tools :refer [tools]]
            [oops.core :refer [ocall]]))

(enable-console-print!)

(defonce state (r/atom {
                        :id 0
                        :appstate {
                          :current-page 1
                          :active-panel nil
                          :tool :panels ; :panels :drawing
                          :drawing? false
                        }
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
           [(/ width  grid-width)
            (/ height grid-height)])

    (fn [state]
      (let [page-num (get-in @state [:appstate :current-page])]
        [:div.root {:key "root"}
         [overview state]
         ^{:key (str "page-" page-num)} [page state]
         [tools state]])
      )
    ))

(rd/render [root state] (ocall js/document :getElementById "app"))

