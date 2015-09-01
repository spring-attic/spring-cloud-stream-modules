Spring Cloud Stream Firehose Module
=============================

In this *Spring Cloud Stream* module, a source emits events from a [doppler](https://github.com/cloudfoundry/loggregator) endpoint

## Requirements

To run this sample, you will need to have installed:

* Java 7 or Above
* A Redis server
* [Cloudfoundry](https://github.com/cloudfoundry/cf-release), [Lattice](http://lattice.cf) or a doppler [simulator](https://github.com/viniciusccarvalho/doppler-simulator) running

## Generated sources

The firehose depends on generated code from [droposonde-protocol](https://github.com/cloudfoundry/dropsonde-protocol). In order to simplify requirements to run this module
all java sources have been generated already.

The [droposonde-protocol](https://github.com/cloudfoundry/dropsonde-protocol) repo has instructions and a script on how to generate java code
from proto files.

This module targets the PCF 1.5 version of the dropsonde protocol [dropsonde-protocol@ff69d48b9c570b0549c9161c8377f610b6e81dda](https://github.com/cloudfoundry/dropsonde-protocol/tree/ff69d48b9c570b0549c9161c8377f610b6e81dda)

## Code Tour

This sample connects to a doppler remote websocket endpoint and emits events from it. The sample converts the binary protocol buffer message into either a Tuple Object or a JSON String, depending on your configuration

* demo/FirehoseApplication: Main boot application, bootstraps websocket clients and SSL contexts
* source/FirehoseSource: Module that connects to the remote websocket. Note that connection needs to happen after the spring context was initialized
* source/ByteByfferMessageConverter: Message converter to read the binary message and convert into protocol buffers and then a tuple or JSON
* source/TupleFactory: Support class to convert from Protobuf pojos into an XD tuple
* source/FirehoseOptionsMetadata: Configuration for this sample
  * dopplerUrl (required) : Remote endpoint of doppler, usually ws://doppler.<domain>
  * cfDomain (cloudfoundry only) :  this is your application domain on cloudfoundry
  * authenticationURL (cloudfoundry only) : UAA authentication endpoint
  * username (cloudfoundry only) : user with doppler rights
  * password (cloudfoundry only) : user credentials
  * dopplerSubscription (optional) : the subscription id, leave it blank for the simulator
  * outputJson (optional) : Output JSON string instead of Tuple
  * trustSelfCerts (optional) : If you are using wss and self signed certs on the doppler endpoint 

## Building with Maven

Build the sample by executing (you need to explicitely remove the imports profile, otherwise build will fail:

	source>$ mvn clean package 

## Running the Sample

To start the source module execute the following:

	source>$ java -jar target/spring-cloud-streams-sample-firehose-1.0.0.BUILD-SNAPSHOT-exec.jar
