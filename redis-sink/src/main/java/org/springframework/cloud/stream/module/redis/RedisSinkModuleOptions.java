package org.springframework.cloud.stream.module.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Used to configure those Redis Sink module options that are not related to connecting to Redis.
 *
 * @author Eric Bottard
 */
@ConfigurationProperties("module.")
public class RedisSinkModuleOptions {

	public String getTopicExpression() {
		return topicExpression;
	}

	public void setTopicExpression(String topicExpression) {
		this.topicExpression = topicExpression;
	}

	private String topicExpression = "'foobar'";
}
