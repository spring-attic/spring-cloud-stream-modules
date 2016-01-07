package org.springframework.cloud.stream.module.log.loggregator.source;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author <a href="josh@joshlong.com">Josh Long</a>
 */
@SpringBootApplication
public class LoggregatorSourceApplication {

    public static void main(String[] args) {
        SpringApplication.run( LoggregatorSourceApplication.class, args);
    }
}
