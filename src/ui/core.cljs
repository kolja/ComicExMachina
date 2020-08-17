(ns ui.core
  (:require [rum.core :as rum]
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
    :pages [{:panels []}]
}))

(rum/defc root < rum/reactive []
  (let [preferences (rum/cursor-in state [:preferences])
        prefs (rum/react preferences)
        all-pages (rum/cursor-in state [:pages])
        page-num (rum/cursor-in state [:current-page])
        current-page (rum/cursor-in state [:pages (rum/react page-num)])]
    (swap! preferences assoc :cell-dimensions [(/ (prefs :width) (prefs :grid-width)) 
                                               (/ (prefs :height) (prefs :grid-height))])
  [:.root
    (overview preferences all-pages page-num)
    (page preferences current-page)]))

(rum/mount (root) (ocall js/document :getElementById "app"))
