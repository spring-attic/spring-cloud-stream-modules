package org.springframework.cloud.stream.module.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * A main application that can be used to run the Http Source as a standalone app.
 *
 * @author Eric Bottard
 */
@SpringBootApplication
public class HttpSourceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HttpSourceApplication.class, args);
	}

}
