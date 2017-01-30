(ns app.utils
  (:import goog.History
           goog.history.EventType))

(def history (History.))
(defn redirect [path]
   (. history (setToken path)))
