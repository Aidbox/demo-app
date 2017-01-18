(ns app.chloroform
  (:require-macros [reagent.ratom :refer [reaction]]
                   [cljs.core.async.macros :as m :refer [go alt!]])
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [cljs-pikaday.reagent :as pikaday]
            [cljs.core.async :as a]
            [cljs-time.format :as f]
            [cljs-time.coerce :refer [from-long to-long from-string]]
            [app.svg :as svg :refer [svg]]
            [reagent.core :as r]))

(defn vectorize-input-name [attrs]
  (update-in attrs [:name] #(if (vector? %) % (vector %))))

(defn form-atom [& [data]] (r/atom {:data (or data {}) :errors {} :touched {} }))

(defn form-cursor [scope path & [data]]
  (swap! scope assoc-in path {:data (or data (get-in @scope (conj path :data)) {}) :errors {} :touched {} })
  (r/cursor scope path))

(defn reset [form & init]
  (swap! form  {:data (or init {}) :errors {} :touched {} } ))

(defn disable [f] (swap! f assoc :disabled true))

(defn enable [f] (swap! f dissoc :disabled))

(defn mark-as-touched [doc key] (swap! doc assoc-in (into [:touched] key) true))

(def validators
  {:$required (fn [cfg x]
                (when (and (not (or (true? x) (false? x)))
                           (or (nil? x) (= x "") (empty? x)))
                  :required))

   :$min-length (fn [min x]
                  (when [min x]
                    (if (or (nil? x) (< (count x) min) (< (.-length x) min))
                      :min-length)))

   :$max-length (fn [max x]
                  (when (and (not (nil? x)) (> (count x) max) (> (.-length x) max))
                    :max-length))})

(defn throttle [t cb]
  (let [a (r/atom nil)]
    (fn [e]
      (.persist e)
      (when @a (.clearTimeout js/window @a))
      (reset! a (.setTimeout js/window  #(cb e) t))
      true)))

(defn bind [doc path & [attrs]]
  (when-not  (get-in @doc path)
    (swap! doc assoc-in path (:value attrs)))
  (merge attrs
         {:on-change #(swap! doc assoc-in path (.. % -target -value))
          :value (get-in @doc path)}))


(defn bind-to [doc key attrs]
  (let [default {:on-change #(if (= (:as attrs) :checkbox)
                               (swap! doc assoc-in (into [:data] key) (.. % -target -checked))
                               (swap! doc assoc-in (into [:data] key) (.. % -target -value)))

                 :on-focus (fn [] (swap! doc assoc :focused true))

                 :on-blur #(do (mark-as-touched doc key)
                               (swap! doc assoc :focused false))

                 :class (str/join " " [(:class attrs)
                                       (when (and (get-in @doc [:errors key])
                                                  (get-in @doc [:touched key])) "error")])}]

    (merge default
           attrs
           (if (= :checkbox (:as attrs))
             {:checked (not (not (get-in @doc (into [:data] key))))}
             {:value (get-in @doc (into [:data] key))}))))

(defn index-of [coll value]
  (first (map first
              (filter #(= (second %) value)
                      (map-indexed vector coll)))))

(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec  (concat  (subvec coll 0 pos)  (subvec coll  (inc pos)))))

(defn checked? [doc key value]
  (let [idx (index-of (get-in @doc key) value)]
    (and (>= idx 0) (not (nil? idx)))))

(defn selected? [doc key value]
  (= (get-in @doc key) value))

(defn box-toggle [doc key value]
  (fn []
    (if (empty? (get-in @doc key))
      (swap! doc assoc-in key []))
    (let [c (checked? doc key value)
          d (get-in @doc key)
          i (index-of (get-in @doc key) value) ]
      (swap! doc assoc-in key
             (if c
               (vec-remove d i)
               (conj d value))))))

(defn box-select [doc key value]
  (fn [] (swap! doc assoc-in [:data key] value)))

(def simple-inputs
  {:string     [:input {:type "text" :class :form-control }]
   :password   [:input {:type "password" :class :form-control}]
   :email      [:input {:type "email" :class  :form-control}]
   :text       nil
   :json       nil
   :sql        nil
   :javascript nil
   :checkbox   nil})

(def cm-modes
  {:sql "text/x-sql"
   :json "application/json" })

(def default-cm-options
  {:lineNumbers true
   :height "auto"
   :viewportMargin js/Infinity})

(defn cm-config [opts]
  (merge default-cm-options opts))

(defn- bind-textarea [element-id textarea doc ks focused-atom]
  ;; update watcher
  (let [watcher-name (keyword (str element-id "-watcher"))
        dispatch-event (fn [ta event-type]
                         (.dispatchEvent ta
                                         (doto (.createEvent js/document "Event")
                                           (.initEvent event-type true false))))]
    (remove-watch doc watcher-name)
    (add-watch doc watcher-name
               (fn [key ra old-val new-val]
                 (let [input-value (get-in new-val ks)]
                   (if input-value
                     (when (not= (.-value textarea) input-value)
                       (aset textarea "value" (.toString input-value)))
                     (aset textarea "value" ""))

                   (dispatch-event textarea "autosize:update")))))

  ;; new event listener with new keys
  (aset textarea "onchange" (fn [& _]
                              (swap! doc assoc-in ks (.-value textarea))))

  ;; setting new value only when 'change' event listener
  ;; was updated
  (aset textarea "value" (str (get-in @doc ks)))

  ;; (dispatch-event textarea "autosize:update")

  (aset textarea "onfocus" (fn [& _]
                             (reset! focused-atom true)
                             (swap! doc assoc :focused true)))

  (aset textarea "onblur" (fn [& _]
                            (do (reset! focused-atom false)
                                (mark-as-touched doc ks)
                                (swap! doc assoc :focused false)))))

(defn textarea-autosize [doc ks options]
  (let [element-id (str "textarea_" (gensym))
        focused-atom (r/atom false)]
    (r/create-class
     {:reagent-render
      (fn [doc ks options] [:div.textarea-wrapper
                            {:id element-id
                             :class (if @focused-atom "focused" "unfocused")}])

      :component-did-mount
      (fn [this]
        (let [argv (aget (aget this "props") "argv")
              [_ doc ks options] argv

              root (.getElementById js/document element-id)
              textarea (.createElement js/document "textarea")]

          (aset textarea "rows" 1)
          (.appendChild root textarea)
          (bind-textarea element-id textarea doc ks focused-atom)
          (js/autosize textarea)))

      :component-will-receive-props
      (fn [this new-argv]
        (.log js/console "TODO: textarea got new propz, but I'm not implemented!"))})))


(defn labelize [k]
  (str/join " " (map str/capitalize (flatten (map (fn [x] (-> x
                                                              name
                                                              (str/split #"_"))) k)))))

(defn mk-simple-input [doc {as :as ks :name ek :extraKeys :as attrs}]
  (let [as (:as attrs)
        attrs (dissoc attrs :as :messages :hint)]
   (cond
     #_(some #(= % as) [:json :sql :javascript])
     #_[codemirror doc (into [:data] (if (vector? ks) ks [ks]))
      (merge default-cm-options {:extraKeys ek :mode (get cm-modes as)})]

     (= :checkbox as)
     [:input (merge (bind-to doc (:name attrs) attrs)
                    {:id (name (str/join "_" (map name (:name attrs))))
                     :type "checkbox"})]

     (= :text as)
     [textarea-autosize doc (into [:data] (if (vector? ks) ks [ks])) {}]

     :else
     (let [[tag def-attrs] (get simple-inputs as)
           attrs (merge (bind-to doc (:name attrs) (merge def-attrs attrs))
                        {:id (name (str/join "_" (map str (:name attrs))))}) ]
       [tag attrs]))))

(defn mk-checkbox [doc {key :name :as attrs}]
  (let [value-fn (or (:value-fn attrs) identity)
        text-fn  (or (:text-fn attrs) identity)
        k (into [:data] key)]

    (into [:div.check-boxes-container]
          (map (fn [x]
                 [:div.check-box
                  {:on-blur #(mark-as-touched doc key)
                   :on-click  (box-toggle doc k  (value-fn x))
                   :class  (when  (checked? doc k  (value-fn x)) "checked") }

                  (if (checked? doc k (value-fn x))
                    [svg :checkbox-checked]
                    [svg :checkbox-unchecked] )

                  [:a.check-box-label {:href "javascript:void(0)" }
                   (text-fn x)]])
               (:collection attrs)))))

(defn mk-radio-buttons [doc {key :name :as attrs}]
  (let [value-fn (or (:value-fn attrs) identity)
        text-fn  (or (:text-fn attrs) identity)
        k (into [:data] key)]

    (into [:div.radio-buttons-container]
          (map (fn [x]
                 [:div.radio-btn {:on-blur #(mark-as-touched doc key)
                                  :on-click #(swap! doc assoc-in k (value-fn x))
                                  :class (when (selected? doc k (value-fn x)) "checked")}

                  (if (= (get-in @doc k) (value-fn x))
                    [svg :radio-checked]
                    [svg :radio-unchecked] )

                  [:a.radio-btn-label {:href "javascript:void(0)"}
                   (text-fn x)]])
               (:collection attrs)))))

(defn mk-select [doc {key :name :as attrs}]
  (let [value-fn (or (:value-fn attrs) identity)
        text-fn  (or (:text-fn attrs) identity)
        k (into [:data] key)]

     (into [:select.form-control {:on-change #(swap! doc assoc-in k (.. % -target -value))
                                  :on-blur #(mark-as-touched doc key)
                                  :value (or (get-in @doc k) "")
                                  :class (:class attrs)
                                  :disabled (:disabled attrs)}]
           (map (fn [x]
                  [:option {:value (or (value-fn x) "")} (text-fn x)])
                (:collection attrs)))))

(defn collection [doc {name :name :as attrs} k]
  (let [key (into [:data] name)]
    (into [:div]
          (map-indexed #(mk-simple-input doc {:name (conj name k %1) :as :string})
                       (get-in @doc (conj key k))))))

(defn human-name [doc {name :name fields :fields :as attrs}]
  (let [key (into [:data] name)
        value (get-in @doc key)]
    (for [[f a] fields] ^{:key f}
      [:div
       [:label.control-label
        (or (:label a) (labelize (:name attrs)))]
       [collection doc attrs f]])))

(defn mk-human-name [doc {name :name :as attrs}]
  (let [key (into [:data] name)
        value (get-in @doc key) ]

    (into [:div.human-names]
          (map-indexed #(human-name doc (merge attrs {:name (conj name %1)})) value))

    ))

(defn mk-date [doc {name :name :as attrs}]
  (let [key (into [:data] name)
        d (r/cursor doc key) ]

    [:div.input-prepend.input-group
     [:span.add-on.input-group-addon [:i.glyph-icon.icon-calendar]]
     [pikaday/date-selector {:date-atom d
                             :pikaday-attrs {:max-date (js/Date.) }
                             :input-attrs {:class :form-control }}]]))


(def custom-inputs
  {:check-boxes mk-checkbox
   :select mk-select
   :human-name mk-human-name
   :date mk-date
   :radio-buttons mk-radio-buttons})

(defn mk-input [doc attrs]
  (let [attrs (vectorize-input-name attrs)]
    (if (contains? simple-inputs (:as attrs))
      [mk-simple-input doc attrs]

      (if-let [custom (get custom-inputs (:as attrs))]
        (custom doc attrs)
        (println "ERROR: do not know how to render" attrs)))))

(defn mk-error [doc attrs]
  (let [*doc @doc
        path (:name attrs)
        errs (->> (get-in *doc (into [:errors] path))
                  (map #(or (get attrs %) (name %)))) ;; TODO: default messages
        srv-errs (or (get-in *doc (into [:server-errors] path)) [])
        errs (concat errs srv-errs)]
    [:div.error-container
     (when (and (or (:submit *doc)
                    (get-in *doc (into [:touched] path)))
                (not (empty? errs)))
       [:div.error (str/join ", " errs)])]))

(def non-dashed-inputs
  #{:radio-buttons :check-boxes :json :javascript :sql})

(defn mk-row [doc attrs]
  (let [attrs (vectorize-input-name attrs)]

    [:div.form-group {:class (str (name (:as attrs))
                                  (when (and (get-in @doc (into [:errors] (:name attrs)))
                                             (or (get-in @doc (into [:touched] (:name attrs)))
                                                 (:submit @doc)))
                                    " error"))}



     [:label.col-sm-3 {:for (str/join "_" (map name (:name attrs))) :class :control-label}
      (when (= :checkbox (:as attrs)) [svg :checkbox-checked])
      (when (= :checkbox (:as attrs)) [svg :checkbox-unchecked])

      (or (:label attrs) (labelize (:name attrs)))]

     [:div.col-sm-6
      [mk-input doc attrs]]

     (when-not (contains? non-dashed-inputs (:as attrs)) [:div.dash])

     [:div.under-ctrl
      (when (:hint attrs)
        [:div.hint (:hint attrs)])
      (mk-error doc attrs)] ]))

(def inputs
  {:$input    mk-input
   :$row      mk-row
   :$error    mk-error})

(defn extract-validators [attrs]
  (reduce (fn [acc [k v]]
            (if (= 0 (.indexOf (name k) "$"))
              (assoc-in acc [:validators k] v)
              (assoc-in acc [:attrs k] v)))
          {:validators {} :attrs {}} attrs))

(defn mk-inputs [doc cnt]
  (let [validators (atom {})
        dom (walk/postwalk
             (fn [x]
               (if-let [inp (and (vector? x) (get inputs (first x)))]
                 (do
                   (let [attrs (vectorize-input-name (second x))
                         {attrs :attrs
                          vals :validators} (extract-validators attrs)]
                     (when-not (empty? vals) (swap! validators assoc (:name attrs) vals))
                     [inp doc attrs]))
                 x))
             cnt)]
    [@validators dom]))

(defn merge-errors [e1 e2]
  (reduce (fn [acc [k v]]
            (if-let [errs (get acc k)]
              (assoc acc k (concat errs v))
              (assoc acc k v))) e1 e2))

(defn validate [doc form-validators validate-fn]
  (let [errors (reduce (fn [acc [key vs]]
                         (let [errs (reduce (fn [a [rule cfg]]
                                              (if-let [err (apply (rule validators) [cfg (get-in @doc (into [:data] key))])]
                                                (conj a err)
                                                a))
                                            [] vs) ]

                           (if (empty? errs)
                             acc
                             (assoc acc key  errs ))))
                       {} form-validators)

        errors (if-let [validate-errors  (and  validate-fn (validate-fn (:data @doc)))]
                 (merge-errors errors validate-errors)
                 errors)

        valid (empty? errors)

        errors (reduce (fn [acc [k v]] (assoc-in acc k v)) {} errors)]

    (if (not= (:errors @doc ) errors)
      (do (swap! doc assoc :errors errors)
          (swap! doc assoc :valid  valid)))))

(defn valid? [doc]
  (empty? (:errors doc)))

(defn on-submit [doc attrs validatee]
  (let [v (:validators attrs)]
    (fn [ev]
      (.preventDefault ev)
      (swap! doc assoc :submit true)
      (validatee)
      (when (valid? @doc)
        (go
          (when-let [result (a/<! ((:on-submit attrs) (:data @doc)))]
            (swap! doc assoc :server-errors (:errors result))))))))


(defn form-classes [cls doc]
  (str/join " " [cls
                 (when (:focused doc) "focused")
                 (when (:empty doc) "empty")
                 (when (:valid doc) "valid")
                 (when (not (:empty doc)) "dirty")]))

(defn reset-form [fatom]
  (reset! fatom {:data {} :errors {} :touched {} }))

(defn form [doc attrs & cnt]
  (let [[vals dom] (mk-inputs doc cnt)
        validation  #(validate doc vals (:validate attrs))
        form-dom (into [:form (merge attrs
                                     {:on-submit (on-submit doc attrs validation)
                                      :class (form-classes (:class attrs) @doc)})]
                       dom)]
    (validation)
    form-dom))
