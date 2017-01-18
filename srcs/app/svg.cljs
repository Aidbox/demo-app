(ns app.svg)

(def svg* {})

(defn svg [k]
  (update-in (k svg*) [1] assoc :class (name k)))
