package org.springframework.cloud.stream.module.jdbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A main application that can be used to run the Jdbc Sink as a standalone app.
 *
 * @author Eric Bottard
 */
@SpringBootApplication
public class JdbcSinkApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(JdbcSinkApplication.class, args);
	}

}
