package org.springframework.cloud.stream.module.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author Waldemar Hummer
 */
@Configuration
@EnableConfigurationProperties(HttpClientProcessorProperties.class)
public class HttpClientProcessorConfiguration {

	@Autowired
	private HttpClientProcessorProperties properties;

	@Bean
	public RestTemplate httpClient() throws Exception {
		RestTemplate t = new RestTemplate();
		return t;
	}

}
