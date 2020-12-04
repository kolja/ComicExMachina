(ns electron.save
  (:require [clojure.string :refer [join replace-first]]
            [oops.core :refer [oget ocall oset!]]
            [goog.string :as gstring]
            [canvas :refer [createCanvas loadImage]]
            [ui.tools :refer [lpad]]
            [tools.devtools :refer [log]]))

(enable-console-print!)

(defonce fs (js/require "fs"))
(defonce ipc (-> (js/require "electron") 
                 (oget :ipcMain)))

(defn save [menuItem browserWindow e]
  (log "electron.save/save was called")
   ;; (ocall browserWindow/webContents :send "request-state")
   )

(defn draw [state pg-id pn-id] 
  (let [panel        (get-in state [:pages pg-id :panels pn-id])

        [[bx by] [bx2 by2]]    (get panel :bounding-box)
        bw           (- bx2 bx)
        bh           (- by2 by)

        p2           (partial lpad "0" 2)
        {:keys [width height export-path]
         [cw ch] :cell-dimensions} 
                     (get-in state [:preferences])

        canvas       (createCanvas (* bw cw) (* bh ch))
        ctx          (ocall canvas :getContext "2d")
        strokes      (join " " (for [{:keys [verts]} (get panel :strokes)]
                                        (str "M" (join "L" (for [[x y] verts] (str x " " y))))))
        filename     (str export-path "page" (p2 pg-id) "/panel" (p2 pn-id) ".png")
        out          (ocall fs :createWriteStream filename)
        stream       (ocall canvas :pngStream)
        ]

    (ocall stream :on "data" (fn [chunk] (ocall out :write chunk)))
    (ocall stream :on "end"  #(log (str filename " saved.")))

    (oset! ctx :lineWidth 2)
    (oset! ctx :strokeStyle "red")
    (oset! ctx :fillStyle "transparent")
    (ocall ctx :fillRect 0 0 width height)
    (ocall ctx :stroke strokes)
    ))

(ocall ipc :handle "save" 
       (fn [e s]
         (let [state (js->clj s :keywordize-keys true)
               pages (get state :pages)]
           (log "hello save!")
           (doseq [[pg-id page] pages]
             (doseq [[pn-id panel] (get page :panels)]
               (draw state (js/parseInt pg-id) (js/parseInt pn-id)) 
               )))))
