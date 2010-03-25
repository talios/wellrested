(ns com.theoryinpractise.wellrested.webapp
  (:gen-class :extends javax.servlet.http.HttpServlet)

  (:use clojure.contrib.json.write)
  (:use compojure.control)
  (:use compojure.html.gen)
  (:use compojure.html.page-helpers)
  (:use compojure.html.form-helpers)
  (:use compojure.http.helpers)
  (:use compojure.http.multipart)
  (:use compojure.http.routes)
  (:use compojure.http.request)
  (:use compojure.http.servlet)
  (:use compojure.http.session)
  (:use compojure.map-utils)
  (:use compojure.validation))

(defmacro OPTIONS "Generate an OPTIONS route."
  [path & body]
  (compile-route :options path body))

(defmacro PATCH "Generate a PATCH route."
  [path & body]
  (compile-route :patch path body))

(defroutes wellrested-app

  (GET "/"
    {:status 200
     :headers {"Content-Type" "application/wellrested+json"}
     :body (json-str {:name "wellrested"
                      :links [{:rel "quicktask" :url "./quicktask1" :type "application/wellrested-task+json" :alt "Do something task"}]})})


  (GET "/quicktask1"
    {:status 200
     :headers {"Content-Type" "application/quicktask+json"
               "Accept-Patch" "application/quicktask-request+json"}
     :body (json-str {:name "quicktask1"})})

  (OPTIONS "/quicktask1"
    {:status 200
     :headers {"Accept-Patch" "application/quicktask-request+json"}})

  (PATCH "/quicktask1"
    (if (= "application/quicktask-request+json" (:content-type request))
      {:status 200 :body (json-str {:status "Patched"})}
      {:status 400 :body (json-str {:status "Unknown patch type"})}))

  (ANY "*" {:status 404}))

(defservice wellrested-app)
