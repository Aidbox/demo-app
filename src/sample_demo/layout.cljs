(ns sample-demo.layout)

(defn layout [content]
  [:div#wrapper
   [:header "HEADEER"]
   [:aside
    [:ul
     [:li [:a {:href "#/user/1"} "user 1"] ]
     [:li [:a {:href "#/user/2"} "user 2"] ]
     [:li [:a {:href "#/user/3"} "user 3"] ]
     [:li [:a {:href "#/user/4"} "user 4"] ]
     [:li [:a {:href "#/user/5"} "user 5"] ]
     [:li [:a {:href "#/user/6"} "user 6"] ]
     [:li [:a {:href "#/user/7"} "user 7"] ]
     [:li [:a {:href "#/user/8"} "user 8"] ]
     ]
    ]
   [:div#content content]
   [:footer "footer"] ])
