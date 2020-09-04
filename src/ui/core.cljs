(ns ui.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [reagent.ratom :as ra]
            [tools.devtools :refer [log]]
            [ui.overview :refer [overview]]
            [ui.page :refer [spread-page]]
            [ui.tools :refer [tools]]
            [oops.core :refer [ocall]]))

(enable-console-print!)

(defonce state (r/atom {
                        :id 0
                        :appstate {
                          :scale 1
                          :current-page 1
                          :active-panel nil
                          :tool :panels ; :panels :drawing
                          :drawing? false
                        }
                        :preferences {
                                      :gutter-width 6
                                      :grid-width 16
                                      :grid-height 16
                                      :margin [2 2 2 2] ; top center bottom outer
                                      :width 210
                                      :height 297
                                      ; :cell-dimensions [:grid-width :grid-height] ; get calculated when app is mounted
                                      }
                        :pages [{}          ;; page "zero" stays blank
                                {:panels [{ :cells [] }] 
                                 :cells {}} ;; empty doc at least contains one page
                                ]}))

(defn root [state]

  (let [preferences                                   (@state :preferences)
        {:keys [width height grid-width grid-height]} preferences ]

    (swap! state assoc-in [:preferences :cell-dimensions]
           [(/ width  grid-width)
            (/ height grid-height)])

    (fn [state]
      (let [{:keys [pages appstate preferneces]} @state
            current-page (appstate :current-page)
            current-spread (quot current-page 2)
            page-height  (preferences :height)
            offset-y (* -1 current-spread page-height)
            ]
       [:div.root {:key "root"}
         [overview state]
         [:div.spreads {:style {:position "relative"
                                :transition "top 0.5s"
                                :top offset-y}}

          (for [[l r] (partition 2 2 [0] (range (count pages)))] ; 0 for padding. Page 0 is always blank. Could be first or last.
            ^{:key (str "page-" l "-" r)} 
            [spread-page state l r])]
         [tools state]])
      )
    ))

(rd/render [root state] (ocall js/document :getElementById "app"))

