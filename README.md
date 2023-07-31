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
you can have a delay when the function is first started and higher costs when
the app would be more intensively used.

## Architecture

The static part could be hosted using an [AppEngine instance](https://cloud.google.com/appengine/docs/standard/java-gen2/runtime).
There is a free tier, which does require some "standard" Java stuff.  For example,
it assumes your code can be run as a servlet.

We could also run it in a [cloud function](https://cloud.google.com/functions),
if we want to.  Or we could split up the API and the UI.  In order to be in line
with the project vision, we should put it all into functions, hidden behind
an API gateway.  But for an application like this, that will have short bursts of
activity interspersed with long periods of inactivity, AppEngine does seem the
more logical choice.

However, maybe in the future we could examine if parts of even the full app
could be deployed as functions.

## Building

Initially, we would use CircleCI to build the thing, but I would like to switch
to [MonkeyCI](https://monkeyci.com) as soon as it's somewhat usable.

For local development we're using [Clojure CLI](https://clojure.org/reference/deps_and_cli),
not because [Leiningen](https://leiningen.org/) is bad, but I want to try something
new and I like the speed of it.  The downside is that it's pretty bare bones.

## Infrastructure

Infrastructure is managed by [Terraform](https://terraform.io), and the configuration
files can be found under [the infra/ folder](infra/).  The build pipeline is also
responsible for deploying any changes to GCP.

## Local Testing

Build the application as an uberjar, and then start it:

```bash
$ clojure -X:jar:uber
$ java -jar target/blog-fn.jar
```

This will start the application at [http://localhost:8080](http://localhost:8080).
When calling the backend you will run into trouble, however, because of CORS.  Since
this will also not be a problem in production, I haven't added it to the backend.
Instead, I made it so that the backend is able to serve the static files as well.
You can find them under `/site/index.html`.  So start the REPL, start the server
by running `(start-server)` and then go to [http://localhost:8081/site/index.html]([http://localhost:8081/site/index.html]).

## Frontend Testing

The frontend is using [shadow-cljs](https://github.com/thheller/shadow-cljs) for ClojureScript
compilation.  For testing, you can either use the built-in tester, or [Kaocha](https://github.com/lambdaisland/kaocha).

For the default tester, start the `test` build:
```bash
$ npx shadow-cljs watch test
# Starts server at http://localhost:8000
```

I originally added Kaocha because I wanted to generate JUnit style output for the
tests, so CircleCI could interpret the results.  But it turns out that it's not
supported for cljs (at least, that I could find).  So I added my own JUnit output
reporter.  But for active development, it does offer a better UI than the default
one.  In order to use it, you need to start [Funnel](https://github.com/lambdaisland/funnel)
first (see the instructions on the [Kaocha-cljs2](https://github.com/lambdaisland/kaocha-cljs2) page).
Then start the `test-kaocha` build:

```bash
$ npx shadow-cljs watch test-kaocha
# Starts server at http://localhost:8002
```

## Deploying

To manually deploy the application, create an uberjar and invoke `gcloud`:
```bash
$ clojure -X:jar:uber
$ gcloud app deploy
```
This will automatically upload the uberjar, static files and the `app.yaml` that
holds the application configuration.

In order to deploy to production, an new tag must be created starting with `v`.
The pipeline number will then be used for the GCP AppEngine version, since GCP does
not support decimal points in the version name.  For example:
```bash
$ git tag -m "Release 1.0" v1.0
$ git push origin v1.0
```

This will start the build pipeline to deploy a new version, called after the pipeline
number.  This makes it easier to retrace the exact code that is deployed.

## More links

- [The GCP appengine Java Github page](https://github.com/GoogleCloudPlatform/appengine-java-standard).

## Copyright

Copyright (c) 2023 by Monkey Projects.