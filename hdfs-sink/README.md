Spring Cloud Stream HDFS Sink
=============================

This module writes each message it receives to HDFS.

## Requirements:

* Java 7 or Above
* Redis running on localhost
* Hadoop (HDFS) running

## Build:

```
$ mvn clean package -s ../.settings.xml
```

## Run:

```
$ java -jar target/hdfs-sink-1.0.0.BUILD-SNAPSHOT-exec.jar --server.port=8081 --spring.hadoop.fsUri=hdfs://<hdfs-host>:8020 --spring.cloud.stream.bindings.input=<name-to-bind-to>
```

Available module options (with defaults):

spring:
  cloud:
    stream:
      module:
        hdfs:
          sink:
            directory: /xd/test
            fileName: data
            fileExtension: txt
            fileUuid: false
            rollover: 1000000000
            overwrite: false
            codec: 
            partitionPath: 
            fsUri: 


