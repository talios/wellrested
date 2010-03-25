Welcome to the wellrested sandbox for a HATEOAS compliant REST API.

## Starting the API

You can start the API by launching the clojure repl:

    $ mvn clojure:repl


## API Entry Point

This will start a web server running on http://localhost:8080


## Media Type: application/wellrested+json

Describes the project and what you can do with the system.

A series of "quicktask" relations are available to the API consumer, these can be found by inspecting
the "links" attribute of the returned JSON document.  This tentatively follows the rules of
JSON-Schema ( http://json-schema.org/ ).

## Media Type: application/quicktask+json

Describes the associated quick task.  Actions against a resource of this type are performed via PATCH
requests.  Viable patch types currently available for a resource are identified via the "Accept-Patch" header.

    {
	  "name"="quicktask1"
	}

Common patches available for quicktasks include:

 * application/quicktask-request+json - request this quicktask to be executed

## Media Type: application/quicktask-request+json

    {
	  "schedule"="PT1M",
	  "reason"="Please process this action"
	}
	
The quick task request contains a schedule and reason for the request.
