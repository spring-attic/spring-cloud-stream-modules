## Netty based Websocket Sink for Spring Cloud Stream
A simple Websocket Sink implementation for SCS.

## Build and Install
Go to the  `spring-cloud-stream-modules` root directory and run the following
command:

```
mvn -s .settings.xml clean package
```

Make sure that you use the provided `.settings.xml` in order to enable the required Spring Maven repos.

## Usage
As with the other SCS modules, there is a single fat JAR that starts the sink implementation:

```
java -jar target/websocket-sink-*-exec.jar --spring.cloud.stream.bindings.input=ticktock
```

The default is to bind the Netty based WebsocketServer to port `9292` and path `/websocket`. Those
defaults can be overridden via commandline arguments.

### Options
The following commmand line arguments are supported:

#### Server Port
Controls the port onto which the Netty server binds. Default is `9292`.
```
--websocketPort=<PORT>
```

#### Path
Controls the path where the WebsocketServer expects Websocket connections. Default is `/websocket`
```
--websocketPath=<PATH>
```
#### SSL
Controls whether the Websocket server should accept SSL connections (i.e. `wss://host:9292/websocket`). The default
is `false`
```
--ssl=<true|false>
```
Please note that currently a self signed certificate is used to create the SSL context.

#### Logging
Controls the loglevel of the underlying Netty loghandler. The default is `WARN`
```
--websocketLoglevel=<trace|debug|info|warn|error>
```
Please note that this has nothing to do with the Spring Boot logging options (i.e. `--logging.level...`)

#### Threads
Controls the number of worker threads used for the Netty event loop. The default is `1`
```
--threads=<N>
```

### Example
To verify that the websocket-sink receives messages from other spring-cloud-stream modules, you can use the
following simple end-to-end setup.


#### Step 1: Start Redis
The default broker that is used is Redis. Normally can start Redis via `redis-server`.

#### Step 2: Deploy a `time-source`
Deploy a <a href="https://github.com/spring-cloud/spring-cloud-stream-modules/tree/master/time-source">time-source</a> via the following command:

```
java -jar target/time-source***-exec.jar --spring.cloud.stream.bindings.output=ticktock --server.port=9191
```

#### Step 3: Deploy a `websocket-sink`

Finally start a websocket-sink in `trace` mode so that you see the messages produced by the `time-source` in the log:

```
java -jar target/websocket-sink-***-exec.jar --spring.cloud.stream.bindings.input=ticktock --server.port=9393 \
	--logging.level.org.springframework.cloud.stream.module.websocket=TRACE
```

You should start seeing log messages in the console where you started the WebsocketSink like this:

```
Handling message: GenericMessage [payload=2015-10-21 12:52:53, headers={id=09ae31e0-a04e-b811-d211-b4d4e75b6f29, timestamp=1445424778065}]
Handling message: GenericMessage [payload=2015-10-21 12:52:54, headers={id=75eaaf30-e5c6-494f-b007-9d5b5b920001, timestamp=1445424778065}]
Handling message: GenericMessage [payload=2015-10-21 12:52:55, headers={id=18b887db-81fc-c634-7a9a-16b1c72de291, timestamp=1445424778066}]
```

### Actuators
There is an `Endpoint` that you can use to access the last `n` messages sent and received. You have to
 enable it by providing `--endpoints.websocketsinktrace.enabled=true`. By default it shows the last 100 messages via the
`http://host:port/websocketsinktrace`. Here is a sample output:

```
 [
   {
    "timestamp": 1445453703508,
    "info": {
      "type": "text",
      "direction": "out",
      "id": "2ff9be50-c9b2-724b-5404-1a6305c033e4",
      "payload": "2015-10-21 20:54:33"
    }
  },
  ...
  {
    "timestamp": 1445453703506,
    "info": {
      "type": "text",
      "direction": "out",
      "id": "2b9dbcaf-c808-084d-a51b-50f617ae6a75",
      "payload": "2015-10-21 20:54:32"
    }
  }
]
```

There is also a simple HTML page where you see forwarded messages in a text area. You can access
it directly via  `http://host:port` in your browser


### Gotchas
For SSL mode (`--ssl=true`) a self signed certificate is used that might cause troubles with some
Websocket clients. In a future release, there will be a `--certificate=mycert.cer` switch to pass a valid (not
self-signed) certificate.
