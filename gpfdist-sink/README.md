Spring Cloud Stream Gpfdist Sink
================================

This module writes each message it receives to Gpfdist.

## Requirements:

* Java 7 or Above
* Redis running on localhost
* Greenplum DB or HAWQ

## Build:

```
$ mvn clean package -s ../.settings.xml
```

## Run:

```
$ java -jar target/hdfs-sink-1.0.0.BUILD-SNAPSHOT-exec.jar --server.port=8081 --spring.cloud.stream.bindings.input=<name-to-bind-to>
```

