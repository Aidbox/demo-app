(ns app.http
  (:require-macros [cljs.core.async.macros :as m :refer [go alt!]])
  (:require [cljs-http.client :as http]
            [reagent.core :as r]
            [app.config :as c]
            [reagent.session :as session]
            [reagent.cookies :as cookies]
            [cljs.core.async :refer [<!]]))

(defn default-params [opts & [over]]
  (merge {:headers {"accept" "application/json"}
          :with-credentials? true
          :json-params (:data opts)}
         over
         opts))

(defn wrap-request-no-decode [request]
  (-> request
     http/wrap-accept
     http/wrap-form-params
     http/wrap-multipart-params
     http/wrap-edn-params
     http/wrap-transit-params
     http/wrap-json-params
     http/wrap-content-type
     http/wrap-query-params
     http/wrap-basic-auth
     http/wrap-oauth
     http/wrap-method
     http/wrap-url
     http/wrap-channel-from-request-map
     http/wrap-default-headers))

(def request-no-decode
  (wrap-request-no-decode cljs-http.core/request))


(defn get-access-token []
  (if-let [access-token (cookies/get :access_token)]
    access-token
    #_(let [{body :body :as res} (<! (http/get (str "/boxes/" box-id)))] ;; GET ACCESS TOKEN FROM LOGIN
        (if (:success res)
          (do (swap! access-tokens assoc box-id body)
              (:access_token body))))))

(defn perform-request [opts]
  (if (:no-decode opts)
    (request-no-decode opts)
    (http/request opts)))

(defn xhr-box [opts]
  (go (let [url (str (:box-url c/config) (:url opts))
            access-token  (get-access-token)
            params (-> opts
                       (cond-> access-token (assoc-in [:query-params :access_token]  access-token))
                       (assoc :url url )) ]

        (<! (perform-request params)))))

(defn xhr [{data :data :as opts}]
  (let [opts (default-params  opts {:body (when data (.stringify js/JSON (clj->js data)))
                                    :query-params (merge (or (:query-params opts) {}) (or (:params opts) {}))})]
    (xhr-box opts)) )

(defn GET [opts]
  (xhr (merge opts {:method :get})))

(defn POST [opts] (xhr (merge opts {:method :post})))

(defn DELETE [opts]
  (xhr (merge opts {:method :delete})))

(defn PUT [opts]
  (xhr (merge opts {:method :put})))

(comment
  (println (<! (GET {:url "boxes"}))))

