#!/usr/bin/env bash

source `dirname $0`/common.sh


function deploy_sample_app(){
    spring jar sample.jar sample.groovy
    cf push -p sample.jar --random-route $CF_APP
}

function test (){
    mvn -DskipITs=false -s ../../.settings.xml -f ../pom.xml clean test
}

validate_cf
deploy_sample_app
test
