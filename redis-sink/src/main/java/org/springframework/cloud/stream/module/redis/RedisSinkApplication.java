package org.springframework.cloud.stream.module.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses= RedisSink.class)
public class RedisSinkApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(RedisSinkApplication.class, args);
	}

}
