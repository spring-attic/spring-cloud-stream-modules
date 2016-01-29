#!/usr/bin/env bash


source `dirname $0`/common.sh

function teardown(){
    echo $CF_APP
    cf delete -f $CF_APP
}

validate_cf
teardown