# Blog As Functions

This is a trial to create a web app using Clojure(Script) but run it as pure OCI
functions.  This means the app would consist of several functions: one for the
frontend (serving the static html, css and js files) and others for the backend
functionality.

This means each of those steps is deployed as a separate function.  The frontend
could be a simple Nginx or Apache image with the static files added.  The backend
endpoints could either be one function per endpoint, or one generic function that
takes parameters to determine the exact functionality to execute.  We could also
use the API gateway to route several endpoints to the same function, assuming the
function is able to determine the path is was invoked with.

## Why?

First of all because I want to know if it's possible.  Consider it _research_.
And second, because I think this is an ideal way to host a low-traffic website
(for example, a personal blog as this) with minimal cost.  The downside is that
you can have a delay when the function is first started.  This makes it important
to ensure that the images are as small as possible.  For Clojure/Java, this means
using GraalVM to create native executables.

For Nginx we could use the [Alpine-Slim](https://hub.docker.com/_/nginx/tags?page=1&name=alpine-slim)
tagged image, which is only about 11MB.  A complicating factor is that the UI
is needed for more files (css, js...).  So maybe just using the free tier VM
with Apache (or Nginx) is easier.

## Architecture

Each part of the application would be a cloud function.  These would be accessible
through the [OCI API gateway](https://docs.oracle.com/en-us/iaas/Content/APIGateway/home.htm).
This would of course be set up in an automatic fashion.  The typical way to configure
it would be through Terraform.  But this still means we would have to maintain the
config files manually.  It would be more logical to tie the gateway configuration
to to build system.  Ideally, the build phase could reconfigure the API gateway
using the code that's being built.

The assets (html, css, js...) could be served by one function (with Nginx, see above),
and the API part by another function, as a Clojure app.  For performance purposes,
we would have to build it into a native application using GraalVM.

Storage would be [OCI object storage](https://docs.oracle.com/en-us/iaas/Content/Object/home.htm).
Each entry could be a file (`EDN` or `JSON`).

This means we need the following:

- The API code in Clojure
- A way to access object storage through, ideally without having to use the OCI Java libs.
- A way to configure the API gateway
- Frontend code in ClojureScript
- Build pipeline to automate all of the above

## Risks

The main risk is that response times would be too slow.  This is of course a simple
web app that is intended for infrequent personal use only, so even then this would
not be a big issue.  But should this turn out to work just fine, we could use this
in other projects as well.

Reducing the number of assets could be a possible optimization.  The web app could
function with minimal dependencies: the landing html page, the ClojureScript js file
and perhaps some images.  Even the css [could be included in the cljs](https://github.com/clj-commons/cljss).

After running initial tests, I have seen that loading the main page, which is
based upon a minimal Nginx image of 11MB, can take between 15 and 20 seconds.
Subsequent calls are a lot faster (< 500ms).  With appropriate caching on the
end of the API gateway, this initial load time could perhaps be lowered.

## Building

Initially, we would use CircleCI to build the thing, but I would like to switch
to [MonkeyCI](https://monkeyci.com) as soon as it's somewhat usable.

## Copyright

Copyright (c) 2023 by Monkey Projects.