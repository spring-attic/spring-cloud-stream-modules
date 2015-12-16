Spring Cloud Stream Time Source
===============================

This module publishes a timestamp on an interval determined by the `fixedDelay` property.

## Requirements:

* Java 7 or Above
* Redis running on localhost

## Classes:

* TimeSourceApplication - the Spring Boot Main Application
* TimeSource - the module that will generate the timestamp and post it as a message
* TimeSourceProperties - defines the configuration properties that are available for the TimeSource
    	 * dateFormat - how to render the current time, using SimpleDateFormat (default: "yyyy-MM-dd HH:mm:ss")
    	 * fixedDelay - time delay between messages (default: 5)
    	 * initialDelay - delay before the first message (default: 1)
    	 * timeUnit - the time unit for the fixed and initial delays (default: "seconds")

## Build:

```
$ mvn clean package
```

## Run:

```
$ java -jar target/time-source-1.0.0.BUILD-SNAPSHOT-exec.jar
```
