#!/usr/bin/env bash

source `dirname $0`/common.sh

function deploy_sample_app(){
    cf push -p app --random-route $CF_APP
}

function test (){
    ../../mvnw -DskipITs=false -s ../../.settings.xml -f ../pom.xml clean test
}

validate_cf
deploy_sample_app
test
