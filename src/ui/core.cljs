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
  (let [prefs (rum/cursor-in state [:preferences])
        all-pages (rum/cursor-in state [:pages])
        page-num (rum/cursor-in state [:current-page])
        current-page (rum/cursor-in state [:pages @page-num])]
  [:.root
    (overview prefs all-pages page-num)
    (page prefs current-page)]))

(rum/mount (root) (ocall js/document :getElementById "app"))
