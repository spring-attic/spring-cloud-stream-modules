Spring Cloud Stream Log Sink
============================

This module logs each message it receives.

## Requirements:

* Java 7 or Above
* Redis running on localhost

## Classes:

* LogSinkApplication - the Spring Boot Main Application
* LogSink - the module that receives the data from the stream and logs it

## Build:

```
$ mvn clean package
```

## Run:

```
$ java -jar target/log-sink-1.0.0.BUILD-SNAPSHOT-exec.jar
```
