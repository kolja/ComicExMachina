(ns electron.core
  (:require electron.menu))

(def electron       (js/require "electron"))
(def app            (.-app electron))
(def browser-window (.-BrowserWindow electron))

(def mac? (= js/process.platform "darwin"))
(def main-window (atom nil))

(defn init-browser []
  (reset! main-window (browser-window.
                        (clj->js {:width 800
                                  :height 600
                                  :backgroundColor "#111"
                                  :icon (str js/__dirname "/public/icon/icon.png")
                                  :webPreferences {:nodeIntegration true }})))

  (.openDevTools ^js/electron.BrowserWindow @main-window)
  (.loadURL ^js/electron.BrowserWindow @main-window (str "file://" js/__dirname "/public/index.html"))
  (.on ^js/electron.BrowserWindow @main-window "closed" #(reset! main-window nil)))

(.on app "window-all-closed" #(when-not mac? (.quit app)))
(.on app "ready" init-browser)
