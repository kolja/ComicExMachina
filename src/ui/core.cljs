(ns ui.core
  (:require [rum.core :as rum]
            [ui.pages :refer [pages]]
            [ui.document :refer [document]]
            [oops.core :refer [ocall]]))

(enable-console-print!)

(defonce state (atom {
    :id 0
    :preferences {
        :grid-width 24
        :grid-height 24
        :width 210
        :height 297
        }
    :pages [{:panels []}]
}))

(rum/defc root []
  [:.root
    (pages state)
    (document state)])

(rum/mount (root) (ocall js/document :getElementById "app"))
