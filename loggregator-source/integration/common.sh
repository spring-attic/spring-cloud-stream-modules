#!/usr/bin/env bash

export CF_USER=$1
export CF_PASSWORD=$2
export CF_ORG=$3
export CF_SPACE=$4
export CF_API=$5
export CF_APP=$6

function install_cf(){
    mkdir -p $HOME/bin
    curl -v -L -o cf.tgz 'https://cli.run.pivotal.io/stable?release=linux64-binary&version=6.13.0&source=github-rel'
    tar zxpf cf.tgz
    mkdir -p $HOME/bin && mv cf $HOME/bin
}

function validate_cf(){

    cf  -v || install_cf

    export PATH=$PATH:$HOME/bin

    cf api $CF_API
    cf auth $CF_USER $CF_PASSWORD
    cf target -o $CF_ORG -s $CF_SPACE
    cf apps
}
