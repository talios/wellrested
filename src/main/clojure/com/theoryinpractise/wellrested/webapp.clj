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

(defroutes wellrested-app

    (ANY "*"
		{:status 200
		 :headers {"Content-Type" "application/wellrested+json"}
		 :body (json-str {:name "wellrested" 
						  :links [{:rel "quicktask" :url "./quicktask1" :type "application/wellrested-task+json" :alt "Do something task"}]}
		
		
		)})
   )

(defservice wellrested-app)
