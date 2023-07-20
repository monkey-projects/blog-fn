#!/bin/bash

# Build uberjar
clojure -X:uberjar
# Prepare war directory
mkdir -p war
cp -R target/classes/WEB-INF war/
mkdir -p war/WEB-INF/lib
cp target/blog-fn.jar war/WEB-INF/lib/
cp -R target/classes/public/* war/
# Run the dev server
java_dev_appserver.sh --port=8080 war/
