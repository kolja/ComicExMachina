(ns ui.core
  (:require [rum.core :as rum]
            [tools.devtools :refer [log]]
            [ui.overview :refer [overview]]
            [ui.page :refer [page]]
            [oops.core :refer [ocall]]))

(enable-console-print!)

(defonce state (atom {
                      :id 0
                      :current-page 1
                      :preferences {
                                    :grid-width 24
                                    :grid-height 24
                                    :width 210
                                    :height 297
                                    }
                      :pages [{}          ;; page "zero" stays blank
                              {:panels [{ :cells [] }] 
                               :cells {}} ;; empty doc at least contains one page
                              ]}))

(rum/defcs root 
  < rum/reactive 
  {:will-mount (fn [rstate] (let [{:keys [width height grid-width grid-height]} 
                                  (@state :preferences)]
                              (swap! state assoc-in [:preferences :cell-dimensions]
                                     [(/ width grid-width)
                                      (/ height grid-height)]))
                    rstate)}
  []
  (let [preferences   (rum/cursor-in state [:preferences])
        prefs         (rum/react preferences)
        all-pages     (rum/cursor-in state [:pages])
        page-num      (rum/cursor-in state [:current-page])
        current-page  (rum/cursor-in state [:pages (rum/react page-num)])]
    [:.root
      (overview preferences all-pages page-num)
      (page     preferences current-page)]))

(rum/mount (root) (ocall js/document :getElementById "app"))

