(ns ui.tools
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [tools.devtools :refer [log]]))

(enable-console-print!)

(defn mouse-click [state new-tool]
  (log new-tool)
  (swap! state update-in [:appstate] assoc 
         :tool new-tool
         :drawing? false))

(defn tools [state]
  (fn [state]

    (let [current-tool (get-in @state [:appstate :tool])
          tools        [{:key :panels :text "panels"}
                        {:key :drawing :text "draw"}]]

      [:div.tools {:key "tools"}
       (for [tool tools]
         [:button {:key (str "tool-" (name (tool :key)))
                   :on-click (partial mouse-click state (tool :key))
                   :class [(when (= (tool :key) current-tool) "current")]} 
          (tool :text)]
       )])))



