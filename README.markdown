Welcome to the wellrested sandbox for a HATEOAS compliant REST API.

The aim of the project is to provide a sample REST API application, that actually conforms to the HATEOAS
REST philosophy: using links, and hypermedia to navigate and drive the application.

## REST Conventions used

Before reading further, some conventions used:

 * Media type distinction is currently handled with "rel" attributes on the standard application/json style media
   types as using application/customthing+json was causing issues with the Compojure web framework.
 * The HTTP method PATCH is being used for non CRUD operations against resources

## Starting the API

You can start the API by launching the clojure repl:

    $ mvn clojure:repl

## API Entry Point

Once the system is running, the primary endpoint of the API can be found at:

    http://localhost:8080

Currently, only one type of resource is currently provided by the API - "the staff list", the availability of this resource
is known when a link with the relationship "stafflist" is returned.

## The Staff List

The staff list resource is available in various different views, different views can be requested by a client by setting
the Accept: header when requesting the resource.  If not set, then the default 

    application/json; rel=wellrested-stafflist

media type will be returned.

### Media Type: application/json; rel=wellrested-stafflist

This is the default staff list media type, it contains a _filtered_ list of active staff members.

The document is a JSON array, with each element containing a series of JSON-Schema link objects, which identify
further things to do with the individual record.

    [
      {
        "links":[
          {
            "rel":"view",
            "url":"http:\/\/localhost:8080\/stafflist\/0c6d5bfc-5e1f-455e-b46d-3b873868519a",
            "type":"application\/wellrested-staffmember+json"
          },{
            "rel":"delete",
            "url":"http:\/\/localhost:8080\/stafflist\/0c6d5bfc-5e1f-455e-b46d-3b873868519a",
            "method":"DELETE"
          }],
        "id":"0c6d5bfc-5e1f-455e-b46d-3b873868519a",
        "name":"Mark",
        "status":"active"
      }
    ]

As we are a small startup organization which doesn't wish to grow too large, if new staff members can be hired, an HTTP
"Link" header for the "create" relation is provided.

### Media Type: application/json; rel=wellrested-fullstafflist

This is a variation of the "application/json; rel=wellrested-stafflist" media type in that deleted staff members are not
removed from the list.

## Sending announcements to staff collections

On resources which allow clients to trigger notifications, the "Accept-Patch" HTTP header will include:

    application/x-www-form-urlencoded; rel=wellrested-announcement

This is a standard URL encoded form containing:

  * message - the message to send

## Creating staff members

Creating staff members is achieved by submitting the following media types where requested:

    application/x-www-form-urlencoded; rel=wellrested-newstaffmember

This is a standard URL encoded form containing the following parameters:

  * name - the new staff members name

If there are too many staff members, then a 400 response will be issued.
