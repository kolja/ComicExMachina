(ns electron.menu)

(def electron (js/require "electron"))

(let [template [{:label (.. electron -app -name) 
                 :submenu [{:label "About" :role "about"}
                           {:label "Preferences"}
                           {:label "Quit" :role "quit"}]}
                {:label "File"
                 :submenu [{:label "open"}
                           {:label "save"}]
                 }]
      Menu     (.-Menu electron)]
  (->> template
       clj->js
       (.buildFromTemplate Menu)
       (.setApplicationMenu Menu)))
