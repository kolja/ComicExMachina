(ns electron.menu
  (:require [electron.save :refer [save]]))

(def electron (js/require "electron"))

(let [template [{:label (.. electron -app -name) 
                 :submenu [{:label "About" :role "about"}
                           {:label "Preferences"}
                           {:label "Quit" :role "quit"}]}

                {:label "File"
                 :submenu [{:label "open" :accelerator "CmdOrCtrl+N"}
                           {:label "save" :accelerator "CmdOrCtrl+S" :click save}]}

                {:label "Tools"
                 :submenu [{:label "panels" :type "checkbox" :checked true :accelerator "esc"}
                           {:label "drawing" :type "checkbox" :accelerator "i"}
                           {:label "move" :type "checkbox"}
                           {:label "delete" :type "checkbox" :accelerator "x"}
                           {:label "bubbles" :type "checkbox"}]}

                {:label "Dev"
                 :submenu [{:label "toggle dev-tools" :role "toggleDevTools"}]}]

      Menu     (.-Menu electron)]

  (->> template
       clj->js
       (.buildFromTemplate Menu)
       (.setApplicationMenu Menu)))
