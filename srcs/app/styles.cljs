(ns app.styles
  (:require [garden.core :as css]
            [clojure.string :as s]
            [gardner.core :as g]
            [garden.color :as c]
            [garden.units :as u]
            ))

(defn $style [rules] (let [st (g/css rules) ]
  [:style {:dangerouslySetInnerHTML {:__html  st}}]))

(defn typography []
  [[:h1 {:$text [2 3]}]
   [:h2 {:font-weight "normal" :$margin [2 0 1.5 0]}]
   [:kbd {:$color [:white :dark-blue]
          :padding "2px 5px"
          :border-radius "5px"
          :display "inline-block"
          :line-height "1.2em"
          :margin "0 5px 0 5px"}]])

(def table
  [:table.table
   {:border-spacing 0 :width "100%"}
   [:thead
    [:th
     {:$text [1 1.5]  :text-align "left" :$border [:bottom :solid 1 :gray]  :$padding [0 0 0 0.5]} ] ]
   [:tbody
    [:tr:hover
     {:$bg-color :true-gray} ]
    [:td
     {:padding "6px 20px 7px 5px"}] ] ])

(def default-form-style
  [:.form
   [:h2 {:font-weight "normal" :$push-bottom 2}]
   [:h3 {:font-weight "bold" :$text 1 :margin-bottom "19px"  :margin-top "19px"}]

   [:.CodeMirror-gutters {:$bg-color :gray} :border "none"]
   [:.CodeMirror-linenumber {:$color :black}]

   [:.form-group
    {:padding-top "22px" :padding-bottom "11px" :min-height "40px" :position "relative"}

    [:&.checkbox
     {:padding-top 0}
     [:label {:position "static"
              :top "auto"
              :display "inline"
              :user-select "none"}]
     [:input {:display "none" :width "auto"}]
     [:.dash {:display "none"}]
     [:span.xradio
      {:border-radius "50%" :border "#333"}
      [:&:checked {:color "green"}]]
     [:svg {:margin-right "10px" :vertical-align "text-top" :cursor "pointer"}]

     [(keyword "input:not(:checked) ~ label svg.checkbox-unchecked") { :display "inline-block" }]
     [(keyword "input:not(:checked) ~ label svg.checkbox-checked") { :display "none" }]
     [(keyword "input:checked ~ label svg.checkbox-checked") { :display "inline-block" }]
     [(keyword "input:checked ~ label svg.checkbox-unchecked") { :display "none" }]]]

   [:label
    {:font-size "13px"
     :line-height "20px"
     :$color :text-gray
     :display "block"
     :position "absolute" :top 0}]

   [:.error
    [:label :.dash {:$color :error :$border-color :error}]
    [:.hint {:display "none"}] ]

   [:.check-boxes :.radio-buttons
    [:label {:$push-bottom 0.5 :$text [1 1.5]}]
    [:.check-box :.radio-btn {:margin-top "2px" :margin-bottom "2px"}
     [:&:hover {:$cursor true }]
     [:svg {:vertical-align "top" :margin-right "10px"}] ] ]

   [:.under-ctrl {:margin-top "5px" :$min-height 1}]
   [:.hint :.error-container
    {:font-size "10px" :line-height "14px"  :$color :text-gray}
    [:.error {:font-size "10px" :$color :error }]]

   [:input :textarea :select :.check-boxes-container :.radio-buttons-container
    {:$text [1 1.5] :width "100%" :$bg-color :transparent}
    [:&:focus {:outline "none"}]
    [:&.error [:&:focus {}]]
    [:&:disabled {:cursor "not-allowed" :opacity 0.8}]]

   [:.search-input
    {:$border [:solid 1 :border-gray] :border-radius "30px" :$padding 0.5  :margin-left "-10px" :padding-left "14px"}]

   [:.check-boxes-container :.radio-buttons-container :.CodeMirror ]

   [(keyword ".dash")
    {:$border [:top :solid 1 :gray] :width "100%"}]
   [(keyword ".dash")
    {:$border [:bottom :solid 1 :transparent]}]
   [(keyword "input:focus ~ .dash") {:$border-color :dark-blue }]
   [(keyword "input:focus ~ label") {:$color :dark-blue }]

   [(keyword "div.textarea-wrapper.focused ~ .dash") {:$border-color :dark-blue }]
   [(keyword "div.textarea-wrapper.focused ~ label") {:$color :dark-blue }]


   [:textarea {:line-height "1.4"
               :padding-top "10px"
               :resize "vertical"
               :padding-bottom "10px"
               :font-family "Hack, monospace"}]])

(def scroll-style
  [[(keyword "::-webkit-scrollbar")
    {:width "12px"
     :background-color  "transparent"
     :border-top  "10px solid transparent" }]
   [(keyword "::-webkit-scrollbar:hover")
    {:background-color "rgba (0, 0, 0, 0.5)"} ]
   [(keyword "::-webkit-scrollbar-thumb:horizontal")
    {:background "rgba(0,0,0,0.2)"
     :border-radius "100px"
     :background-clip "padding-box"
     :border "2px solid transparent"
     :min-width "10px" }]
   [(keyword "::-webkit-scrollbar-thumb:horizontal:active")
    { :-webkit-border-radius "100px"}]
   [(keyword "::-webkit-scrollbar-thumb:vertical")
    {:background "rgba(0,0,0,0.2)"
     :border-radius "100px"
     :background-clip "padding-box"
     :border "2px solid transparent"
     :min-height "10px" }]
   [(keyword "::-webkit-scrollbar-thumb:vertical:active")
    { :-webkit-border-radius "100px"}]])

(defn lighten [c a] (g/lighten c a))
(defn darken [c a] (g/darken c a))
(defn c [k] (g/&c c))

(defn gen-button [selector clr & cnt]
  (let [amount 7]
    [selector
     {:$color clr
      :display "inline-block"
      :$text [1.2 2.5]
      :border-radius "8px"
      :text-align "center"
      :$border (if (first cnt) [1 (first cnt)] [1 (second clr)])
      :width "auto"
      :$margin [0.25 0.5 0.25 0]
      :min-width "114px"}
     [:&:hover {:text-decoration "none"
                :color             (g/darken (first clr) amount)
                :background-color  (if (= :white (second clr ))
                                     (g/lighten :dark-blue 37)
                                     (g/darken (second clr) amount))
                :$cursor [] }]

     [:&:active {:color             (g/darken (first clr) (* 2 amount))
                 :background-color  (if (= :white (second clr ))
                                      (g/lighten :dark-blue 34)
                                      (g/darken (second clr) (* 2 amount))) }]

     [:&:active :&:focus {:outline "none"
                          :box-shadow "inset 0 1px 1px rgba(0,0,0,.075),0 0 8px rgba(102,175,233,.6)"}]]))

(defn gen-label [selector clr]
  (let [amount 7]
    [selector
     {:$color clr
      :display "inline-block"
      :$text [0.85 1]
      :padding "3px 10px"

      :border-radius "4px"}]))

(defn buttons-style []
  [(gen-button :.btn-default [:black :gray])
   (gen-button :.btn-primary [:white :dark-blue])
   (gen-button :.btn-success [:white :green])
   (gen-button :.btn-info    [:dark-blue :white] :dark-blue)
   (gen-button :.btn-warning [:white :yellow])
   (gen-button :.btn-danger  [:white :light-red])
   [:.btn-block {:width "100%"}]

   [:.cool-btn {:text-transform "uppercase" :display "inline-block" :$border [1 :dark-blue]
                :overflow "visible" :border-radius "20px"
                :$bg-color :dark-blue
                :color "white"
                :padding "2px 10px"
                :$cursor true}

    [:svg {:width "14px" :height "14px" :fill "white" :margin-left "5px"
           :vertical-align "sub"}]

    [:&:hover {:background-color (lighten :dark-blue 5)}]
    [:&:active {:outline "none" :background-color (darken :dark-blue 10)}]
    [:&:focus {:outline "none"}]

    [:&.btn-white {:$bg-color :white
                   :color "#5b97de"}
     [:.icon {:margin-right "5px"}]

     [:&:hover {:background-color (lighten :dark-blue 30)}]
     [:&:active {:outline "none" :$bg-color :dark-blue :color "white"}]
     [:&:focus {:outline "none"}]]]

   [(keyword "label:nth-child(2):nth-last-child(6) ~ input:checked:nth-child(5) + label ~ a")
    {:color "black"}]

   [:.switch {:display "inline-block"
              :position "relative"
              :$border [1 :light-gray]
              :$height 1.5
              :overflow "visible"
              :border-radius "10px"}

    [:span.option {:height "23px"
                   :$padding [0 2]
                   :text-transform "uppercase"
                   :$color :light-gray
                   :display "inline-block"
                   :position "relative"
                   :z-index 2
                   :width "50%"
                   :text-align "center"
                   :box-sizing "border-box"
                   :transition "all 0.3s ease-out"
                   :$cursor true
                   :-webkit-user-select "none"
                   :-moz-user-select "none"}

     [:&.active {:$color :white}]]

    [:a.hl {:$bg-color :dark-blue
            :$color :white
            :display "inline-block"
            :position "absolute"
            :margin "-1px -1px"
            :z-index 1
            :height "23px"
            :width "51%"
            :border-radius "11px"
            :box-sizing "border-box"
            :transition "all 0.3s ease-out"}]

    [(keyword ".option:nth-child(1).active ~ .hl") {:left "0"}]
    [(keyword ".option:nth-child(2).active ~ .hl") {:left "50%"}]]])

(defn gen-alert [selector clr]
  [selector {:color (g/darken clr 20)
             :width "auto"
             :$padding [1]
             :border-radius "7px"
             :$border [1 (g/lighten clr 28)]
             :background-color (g/lighten clr 33)
             :$margin [1 0]}])

(defn alerts-style []
  [(gen-alert :.alert-success :green)
   (gen-alert :.alert-info    :dark-blue)
   (gen-alert :.alert-warning :yellow)
   (gen-alert :.alert-danger  :light-red)])

(defn labels-style []
  [(gen-label :.label-success [:white :green])
   (gen-label :.label-info [:white :dark-blue])
   (gen-label :.label-warning [:black :yellow])
   (gen-label :.label-danger [:white :light-red])])

(defn default-style []
  [[:html {:height "100%"}]
   [:#app {:height "100%"}]
   [:body {:height "100%" }] ])

(defn &unstyle []
  [:& {:text-decoration "none"}
   :&:hover {:text-decoration "none"}])

(g/config
 {:vars {:v 10
         :h 10
         :$glow-shadow "0 0 30px rgba(0,0,0,.075)"
         :$noise-bg    "#f8f8f8 url(/b8ad8a862001b8ea95ddafd8af5e7c51.png) center no-repeat"}

  :colors {:mute "#999"
           :dark  "#333"
           :white "#ffffff"
           :gray  "#ddd"
           :blue-gray "#acc0d8"
           :true-gray "#fafcfe"
           :success  "#74b74c"
           :error  "#e3647e"
           :red    "#e3647e"
           :deep-red "#a00"
           :deep-green "#0a0"
           :deep-blue "#00a"
           :indicator "#fbfbfb"
           :history      "#fafafa"
           :light-gray   "#bdbdbd"
           :bracket      "#6e6e6e"
           :dark-gray    "#2f323a"
           :text-gray    "#9a9a9a"
           :br-dark-gray "#636363"
           :text-dark-gray "#636363"
           :bg-gray      "#edeff5"
           :border-gray  "#c6c5c5"
           :table-hover-gray "#eaeaea"
           :table-text-gray "#737373"
           :green        "#74b74c"
           :light-red    "#e3647e"
           :black        "#000"
           :user-hover   "#f0f0f0"
           :yellow       "#ffa929"
           :border-red    "#c2556b"
           :dark-blue    "#5b97de"
           :dark-blue-hover   "#bdd5f2"
           :darkgray     "#7b8087"
           :modal-bg     "rgba(73, 79, 91, 1)"
           :transparent  "transparent" }

  :macros {:&unstyle &unstyle
           :$fixed (fn [t r b l]
                     {:position "fixed"
                      :top    (when t (g/&v t))
                      :right  (when r (g/&h r))
                      :bottom (when b (g/&v b))
                      :left   (when l (g/&h l)) })
           :$block-left (fn [w]
                          {:position "fixed"
                           :top 0 :bottom 0 :left 0
                           :width (g/&h w)})

           :$block-top (fn [h l r]
                         {:position "fixed"
                          :top 0
                          :left   (g/&h l)
                          :right  (g/&h r)
                          :height (g/&v h)  })

           :$circle (fn [] {:border-radius "50%" })

           :$flex-full (fn [] {:flex-grow 1})

           :$flex-self-stretch (fn [] {:align-self "stretch"})


           :$flex-column (fn
                           ([] {:display ["flex" ]
                                :justify-content "space-between"
                                :flex-direction "column"
                                :align-items "baseline"})
                           ([a] {:display ["flex" ]
                                 :justify-content "space-between"
                                 :flex-direction "column"
                                 :align-items a})
                           ([a j] {:display ["flex" ]
                                   :justify-content j
                                   :flex-direction "column"
                                   :align-items a}))

           :$flex-row (fn
                        ([]
                         {:display "flex"
                          :justify-content "space-between"
                          :align-items "baseline"})
                        ([a]
                         {:display "flex"
                          :justify-content "space-between"
                          :align-items a})
                        ([a j]
                         {:display "flex"
                          :justify-content j
                          :align-items a})
                        ([a j ac]
                         {:display "flex"
                          :justify-content j
                          :align-items a
                          :align-content ac }))

           :$flex-row-left (fn []
                             {:display "flex"
                              :justify-content "flex-start"
                              :align-items "baseline"})

           :$line-height (fn [h] {:line-height (g/&v h)})

           :$min-height (fn [h] {:min-height (g/&v h)})

           :$center (fn []
                      {:display "flex"
                       :align-items "center"
                       :justify-content "center"
                       :flex-direction "column" } )

           :$center-block (fn
                            ([w] {:width (g/&h w)
                                  :height (g/&h w)
                                  :min-width (g/&h w)
                                  :min-height (g/&h w)
                                  :display "flex"
                                  :align-items "center"
                                  :justify-content "center"
                                  :flex-direction "column" })
                            ([w h] {:width  (when w (g/&h w))
                                    :height (when h (g/&v h))
                                    :display "flex"
                                    :align-items "center"
                                    :justify-content "center"
                                    :flex-direction "column" }))

           :$box (fn
                   ([w] {:display "block"
                         :width  (g/&h w)
                         :height (g/&h w) })

                   ([w h] {:display "block"
                           :width  (when-not (= w nil) (g/&h w))
                           :height (when-not (= h nil) (g/&v h)) }) )

           :$block-main (fn [t r b l]
                          {:position "fixed"
                           :top    (g/&v t)
                           :right  (g/&h r)
                           :bottom (g/&v b)
                           :left   (g/&h l)})

           :$block-bottom (fn [h l r]
                            {:position "fixed"
                             :bottom 0
                             :left   (g/&h l)
                             :right  (g/&h r)
                             :height (g/&v h)})
           :$cursor (fn [] {:cursor "pointer"})}})
