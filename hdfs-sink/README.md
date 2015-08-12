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

NOTE: This module can have it's runtime dependencies provided during startup if you would like to use a Hadoop distribution other than the default one.

Available module options (with defaults):

    directory: /xd/test
    fileName: data
    fileExtension: txt
    fileUuid: false
    rollover: 1000000000
    overwrite: false
    codec: 
    partitionPath: 
    fsUri: 


