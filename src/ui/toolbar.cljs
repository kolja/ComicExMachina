(ns ui.toolbar
  (:require 
    [reagent.core :as r]
    [reagent.dom :as rd]
    [oops.core :refer [oget ocall]]
    [tools.devtools :refer [log]]))

(enable-console-print!)

(defonce position (r/atom [10 10]))
(defonce dragging? (r/atom false))

(defn offset [e] [(oget e :pageX) (oget e :pageY)])

(defn start-drag [e]
  (reset! dragging? true))

(defn stop-drag [e]
  (reset! dragging? false))

(defn tool-drag [e]
  (when @dragging? (reset! position (offset e))))

(defn mouse-click [state new-tool]
  (swap! state update-in [:appstate] assoc 
         :tool new-tool
         :drawing? false))

(defn toolbar [state]
  (fn [state]

    (let [current-tool (get-in @state [:appstate :tool])

          app-element  (ocall js/document :getElementById "app")

          tools        {:panels  {:text "panels" :icon "library_add.svg"}
                        :drawing {:text "draw"   :icon "create.svg"}
                        :delete  {:text "clear"  :icon "clear.svg"}
                        :move    {:text "hand"   :icon "pan_tool.svg"}
                        :bubble  {:text "bubble" :icon "chat_bubble_outline.svg"}
                        :box     {:text "box"    :icon "crop_din.svg"}
                        :add     {:text "add"    :icon "add_box.svg"}
                        :remove  {:text "remove" :icon "remove_circle_outline.svg"}
                        :stack   {:text "stack"  :icon "filter_none.svg"}}

          toolbar       [:panels :drawing :move :delete :bubble]

          color         {:bg "#333"
                         :bg-current "#444"
                         :icon "#666"
                         :icon-current "#ccc"}]

      (.addEventListener app-element "mousemove" (partial tool-drag))
      (.addEventListener app-element "mouseup" (partial stop-drag))

      [:div.toolbar {:key "tools"
                     :style {
                             :-webkit-box-shadow "1px 1px 0px 5px rgba(0,0,0,0.14)";
                             :background-color "#444"
                             :position "absolute"
                             :display "flex"
                             :top (get @position 1)
                             :left (get @position 0)
                             }}

       [:div.handle {
                     :on-mouse-down (partial start-drag)
                     :style {
                             :background-color "#111"
                             :-webkit-mask (str "url(" js/__dirname "/img/drag_indicator.svg)")
                             :cursor "-webkit-grab"
                             :width 25
                             :height 25}}]

       (for [t toolbar :let [{:keys [text icon]} (get tools t)
                            current?             (= t current-tool)]]

         
         [:div.tool {
                     :key (str "tool-" (name t))
                     :on-click (partial mouse-click state t)
                     :class [(when current? "current")] ;; delete this?
                     :style {
                             :width 25
                             :height 25
                             :background-color (get color (if current? :bg-current :bg))}
                     }
          [:button {
                    :style {
                            :background-color  (get color (if current? :icon-current :icon))
                            :-webkit-mask (str "url(" js/__dirname "/img/" icon ")")
                            :cursor "pointer"
                            :width 25
                            :height 25
                            }
                    }]] 
       )])))



