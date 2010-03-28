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

; Link header info: http://tools.ietf.org/html/draft-nottingham-http-link-header-07

;[
;	{
;		"links":[
;			{
;				"rel":"view",
;				"url":"http:\/\/localhost:8080\/stafflist\/0c6d5bfc-5e1f-455e-b46d-3b873868519a",
;				"type":"application\/wellrested-staffmember+json"
;			},{
;				"rel":"delete",
;				"url":"http:\/\/localhost:8080\/stafflist\/0c6d5bfc-5e1f-455e-b46d-3b873868519a",
;				"method":"DELETE"
;			}],
;		"id":"0c6d5bfc-5e1f-455e-b46d-3b873868519a",
;		"name":"Mark",
;		"status":"active"
;	}
;]

; View Root: curl -v http://localhost:8080/
; Add Staff: curl -v -X POST -H "Content-Type: application/x-www-form-urlencoded; rel=wellrested-newstaffmember" -d name="Test 2" http://localhost:8080/stafflist
; View staff: curl http://localhost:8080/stafflist/0c6d5bfc-5e1f-455e-b46d-3b873868519a
; Delete staff: curl -X DELETE http://localhost:8080/stafflist/0c6d5bfc-5e1f-455e-b46d-3b873868519a
; Send announcement: curl -X PATCH -H "Content-Type: application/x-www-form-urlencoded; rel=wellrested-announcement" -d message="Hello Staff Members" http://localhost:8080/stafflist


; Initial stuff
(def *MAXSTAFF* 5)
(def *STAFFLIST* (ref [{:id "0c6d5bfc-5e1f-455e-b46d-3b873868519a" :name "Mark" :status :active}
                       {:id "71b78f5a-0607-45b7-891f-f092de885737" :name "Edwin" :status :deleted}]))

(defn add-staff-links
  "Add :link elements to a staff member record"
  [staff-member-map]
  (conj staff-member-map {:links [{:rel "view" :url (str "http://localhost:8080/stafflist/" (:id staff-member-map)) :type "application/json; rel=wellrested-staffmember"}
                                  {:rel "delete" :url (str "http://localhost:8080/stafflist/" (:id staff-member-map)) :method "DELETE"}]}))

(defn delete-staff-member
  "Delete a staff member"
  [id]
  (fn [staff-member-map]
    (if (= id (:id staff-member-map))
      (assoc staff-member-map :status :deleted)
      staff-member-map)))

(defn get-active-staff
  []
  (filter #(= :active (:status %)) @*STAFFLIST*))

(defn can-add-staff?
  []
  (< (count (get-active-staff)) *MAXSTAFF*))

(defn get-valid-stafflist-links
  []
  (if (can-add-staff?)
    {"Link" "<http://localhost:8080/stafflist>; rel=\"create\"; type=\"application/x-www-form-urlencoded; rel=wellrested-newstaffmember\"; title=\"Create a new staff member\"; method=\"POST\""}
    {}))

(defn get-valid-stafflist-headers
  []
  (conj {"Content-Type" "application/json; rel=wellrested-stafflist"
         "Accept-Patch" "application/x-www-form-urlencoded; rel=wellrested-announcement"} (get-valid-stafflist-links)))

(defn send-staff-annoucement
  [message]
  (fn [staff-member-map]
    (println (str "Sending '" message "' to " (:name staff-member-map)))))

(defroutes wellrested-app

  (GET "/"
    {:status 200
     :headers {"Content-Type" "application/json; rel=wellrested"
               "Link" "<http://localhost:8080/stafflist>; rel=\"stafflist\"; type=\"application/json\"; title=\"List current staff members\""
               "Link" "<http://localhost:8080/stafflist>; rel=\"stafflist\"; type=\"application/json; rel=wellrested-fullstafflist\"; title=\"List all staff members including deleted\""}
     :body (json-str {:name "wellrested"})})

  (OPTIONS "/stafflist"
    {:status 200
     :headers (get-valid-stafflist-headers)})

  (GET "/stafflist"
    (if (= "application/json; rel=wellrested-fullstafflist" (get (:headers request) "accept"))
      {:status 200
       :headers (get-valid-stafflist-headers)
       :body (json-str (map add-staff-links @*STAFFLIST*))}
      {:status 200
       :headers (get-valid-stafflist-headers)
       :body (json-str (map add-staff-links (get-active-staff)))}))

  (POST "/stafflist"
    (if (= "application/x-www-form-urlencoded; rel=wellrested-newstaffmember" (:content-type request))
      (if (can-add-staff?)
        (dosync
          (let [new-id (.toString (java.util.UUID/randomUUID))]
            (ref-set *STAFFLIST* (conj @*STAFFLIST* {:id new-id :name (:name params) :status :active}))
            {:status 201
             :location (str "http://localhost:8080/stafflist/" new-id)
             :body (json-str {:status "Created"})}))
        {:status 400 :body (json-str {:status "Too many staff members!"})})
      {:status 400 :body (json-str {:status "Unknown content type"})}))

  (PATCH "/stafflist"
    (if (= "application/x-www-form-urlencoded; rel=wellrested-announcement" (:content-type request))
      (map (send-staff-annoucement (:message params)) (get-active-staff) )
      {:status 400 :body (json-str {:status "Unknown content type"})}
      )

    )

  (GET "/stafflist/:id"
    {:status 200
     :headers {"Content-Type" "application/json; rel=wellrested-staffmember"}
     :body (json-str (add-staff-links (first (filter #(= (:id params) (:id %)) @*STAFFLIST*))))})

  (DELETE "/stafflist/:id"
    (dosync
      (ref-set *STAFFLIST* (map (delete-staff-member (:id params)) @*STAFFLIST*))
      {:status 200 :body (json-str {:status "Deleted"})}))


  ; "Accept-Patch" "application/quicktask-request+json"

  ;  (OPTIONS "/quicktask1"
  ;    {:status 200
  ;     :headers {"Accept-Patch" "application/quicktask-request+json"}})

  ;  (PATCH "/quicktask1"
  ;    (if (= "application/quicktask-request+json" (:content-type request))
  ;      {:status 200 :body (json-str {:status "Patched"})}
  ;      {:status 400 :body (json-str {:status "Unknown patch type"})}))

  (ANY "*" {:status 404}))

(defservice wellrested-app)
