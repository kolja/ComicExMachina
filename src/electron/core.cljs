(ns electron.core
  (:require electron.menu
            [oops.core :refer [ocall oget oset!]]))

(def electron       (js/require "electron"))
(def app            (.-app electron))
(def ipc-main       (oget electron :ipcMain))
(def session        (.-session electron))
(def browser-window (.-BrowserWindow electron))

(def mac? (= js/process.platform "darwin"))
(def main-window (atom nil))

(ocall ipc-main :handle "server-hello" (fn [e & args]
  (ocall js/console :log (str args ": Server Hello!"))))

(defn hello [] "hello world")

(defn init-browser []
  (reset! main-window (browser-window.
                        (clj->js {:width 800
                                  :height 600
                                  :backgroundColor "#111"
                                  :icon (str js/__dirname "/public/icon/icon.png")
                                  :webPreferences {:nodeIntegration true }}))) ;; <-- do I really need this?

  ;; (ocall (.-webContents @main-window) :openDevTools)

  ;(-> (ocall session "defaultSession.loadExtension" "/Users/kolja/Library/Application Support/Google/Chrome/Default/Extensions/fmkadmapgofadopljbjfkapdkoienihi/4.8.2_0")
  ;    (.then (ocall (.-webContents @main-window) :openDevTools)))

  (.openDevTools ^js/electron.BrowserWindow @main-window)

  (.loadURL ^js/electron.BrowserWindow @main-window (str "file://" js/__dirname "/public/index.html"))
  (.on ^js/electron.BrowserWindow @main-window "closed" #(reset! main-window nil)))


; (oset! js/process.env "!ELECTRON_DISABLE_SECURITY_WARNINGS" true) ;; not working
(.on app "window-all-closed" #(when-not mac? (.quit app)))
(.on app "ready" init-browser)
