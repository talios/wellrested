(ns com.theoryinpractise.wellrested.jetty
  (:use clojure.contrib.logging)
  (:use compojure)
  (:use com.theoryinpractise.wellrested.webapp))

(when-not *compile-files*
  (info "Starting server")
  (run-server {:port 8080} "/*" (servlet wellrested-app)))
