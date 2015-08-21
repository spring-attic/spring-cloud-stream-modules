package org.springframework.cloud.stream.module.redis.sink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedisSinkApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(RedisSinkApplication.class, args);
    }

}
