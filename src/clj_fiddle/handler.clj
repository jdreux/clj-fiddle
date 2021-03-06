(ns clj-fiddle.handler
  (:require [compojure.core :refer [defroutes context]]
            [clj-fiddle.routes.api :refer [api-routes]]
            [noir.util.middleware :as middleware]
            [ring.middleware.json :as json-middleware]
            [compojure.route :as route]
            [compojure.handler :refer [api]]
            [taoensso.timbre :as timbre]
            [com.postspectacular.rotor :as rotor]
            [environ.core :refer [env]]))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info
     :enabled? true
     :async? false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn rotor/append})

  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "clj_fiddle.log" :max-size (* 512 1024) :backlog 10})

  (timbre/info "clj-fiddle started successfully"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "clj-fiddle is shutting down..."))


;Custom middlewares
(defn wrap-dir-index [handler]
  (fn [req]
    (handler
     (update-in req [:uri]
                #(if (= "/" %) "/index.html" %)))))

(defn wrap-error [handler]
  (fn [request]
    (try
      (handler request)
      (catch java.lang.Throwable e
        (timbre/error e))))
  handler)

(defroutes app-routes
  (context "/api" [] api-routes)
  (route/resources "/")
  (route/not-found "Not Found"))

;; (def app (middleware/app-handler
;;            ;; add your application routes here
;;            [api-routes app-routes]
;;            ;; add custom middleware here
;;            :middleware [wrap-error wrap-dir-index middleware/wrap-strip-trailing-slash]
;;            ;; add access rules here
;;            :access-rules []
;;            ;; serialize/deserialize the following data formats
;;            ;; available formats:
;;            ;; :json :json-kw :yaml :yaml-kw :edn :yaml-in-html
;;            :formats [:json-kw :edn]))




(def app (-> app-routes
             wrap-error
             api
             json-middleware/wrap-json-body
             json-middleware/wrap-json-params
             json-middleware/wrap-json-response
             wrap-dir-index
             middleware/wrap-strip-trailing-slash))
