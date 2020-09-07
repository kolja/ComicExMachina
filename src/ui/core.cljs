(ns ui.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [reagent.ratom :as ra]
            [tools.devtools :refer [log]]
            [ui.overview :refer [overview]]
            [ui.page :refer [spread-page]]
            [ui.toolbar :refer [toolbar]]
            [oops.core :refer [oget ocall]]))

(enable-console-print!)

(defonce state (r/atom {
                        :id 0
                        :appstate {
                          :scale 1
                          :current-page 1
                          :active-panel nil
                          :tool :panels ; :panels :drawing :delete :move :bubble
                          :active? false
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

(defn mouse-wheel [appstate e]
  (let [s             (oget e :deltaX)
        factor        (/ 1 500)
        current-scale (get @appstate :scale)
        new-scale     (+ current-scale (* factor s))
        ]
    (when (< 0.3 new-scale 5)
      (swap! appstate assoc :scale new-scale))
    ))

(defn root [state]

  (let [preferences                                   (@state :preferences)
        {:keys [width height grid-width grid-height]} preferences ]

    (swap! state assoc-in [:preferences :cell-dimensions] ;; TODO: consider to do this inside the render function
           [(/ width  grid-width)
            (/ height grid-height)])

    (fn [state]
      (let [appstate                    (r/cursor state [:appstate])
            scale                       (get @appstate :scale)
            {:keys [pages preferneces]} @state
            current-page                (@appstate :current-page)
            current-spread              (quot current-page 2)
            page-height                 (preferences :height)
            offset-y                    (* -1 scale current-spread (+ 5 page-height))
            ]
       [:div.root {:key "root"
                   :on-wheel (partial mouse-wheel appstate)}
         [overview state]
         [:div.spreads {:style {:position "relative"
                                :transition "top 0.5s"
                                :top offset-y}}

          (for [[l r] (partition 2 2 [0] (range (count pages)))] ; 0 for padding. Page 0 is always blank. Could be first or last.
            ^{:key (str "page-" l "-" r)} 
            [spread-page state l r])]
         [toolbar state]])
      )
    ))

(rd/render [root state] (ocall js/document :getElementById "app"))

