Spring Cloud Stream File Sink
=============================

This module writes each message it receives to a file.

## Requirements:

* Java 7 or Above
* Redis running on localhost

## Classes:

* FileSinkApplication - the Spring Boot Main Application
* FileSinkConfiguration - configures the FileWritingMessageHandler bean
* FileSinkProperties - properties for the file sink
	- binary (default = false)
	- charset (default = "UTF-8")
	- dir (default = "/tmp/dataflow/output");
	- dirExpression (no default; mutually exclusive with an explicit 'dir')
	- mode (the FileExistsMode; default = APPEND)
	- name (default = "file-sink")
	- nameExpression (no default; mutually exclusive with an explicit 'name')
	- suffix (no default)

## Build:

```
$ mvn clean package
```

## Run:

```
$ java -jar target/file-sink-1.0.0.BUILD-SNAPSHOT-exec.jar
```
