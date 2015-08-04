package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.cloud.stream.module.redis.RedisSink;

@SpringBootApplication
@ComponentScan(basePackageClasses= RedisSink.class)
public class SinkApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(SinkApplication.class, args);
	}

}
