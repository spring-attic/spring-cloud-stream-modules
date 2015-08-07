package org.springframework.cloud.stream.module.redis;

import java.util.Objects;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

/**
 * Used to configure those Redis Sink module options that are not related to connecting to Redis.
 *
 * @author Eric Bottard
 */
@ConfigurationProperties(prefix = "spring.cloud.stream.module.redis.sink")
public class RedisSinkProperties {

	private SpelExpressionParser parser = new SpelExpressionParser();

	/**
	 * A SpEL expression to use for topic.
	 */
	private String topicExpression;

	/**
	 * A SpEL expression to use for queue.
	 */
	private String queueExpression;

	/**
	 * A SpEL expression to use for storing to a key.
	 */
	private String keyExpression;

	/**
	 * A literal key name to use when storing to a key.
	 */
	private String key;

	/**
	 * A literal queue name to use when storing in a queue.
	 */
	private String queue;

	/**
	 * A literal topic name to use when publishing to a topic.
	 */
	private String topic;

	public void setKey(String key) {
		this.key = key;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Expression getKeyExpression() {
		return key != null ? new LiteralExpression(key) : parser.parseExpression(keyExpression);
	}

	public void setKeyExpression(String keyExpression) {
		this.keyExpression = keyExpression;
	}

	public Expression getQueueExpression() {
		return queue != null ? new LiteralExpression(queue) : parser.parseExpression(queueExpression);
	}

	public void setQueueExpression(String queueExpression) {
		this.queueExpression = queueExpression;
	}

	public Expression getTopicExpression() {
		return topic != null ? new LiteralExpression(topic) : parser.parseExpression(topicExpression);
	}

	public void setTopicExpression(String topicExpression) {
		this.topicExpression = topicExpression;
	}

	public boolean isKey() {
		return StringUtils.hasText(key) || StringUtils.hasText(keyExpression);
	}

	public boolean isQueue() {
		return StringUtils.hasText(queue) || StringUtils.hasText(queueExpression);
	}

	public boolean isTopic() {
		return StringUtils.hasText(topic) || StringUtils.hasText(topicExpression);
	}

	private String getKey() {
		return key;
	}

	private String getQueue() {
		return queue;
	}

	private String getTopic() {
		return topic;
	}


	// The javabean property name is what will be reported in case of violation. Make it meaningful
	@AssertTrue(message = "Exactly one of 'queue', 'queueExpression', 'key', 'keyExpression', 'topic' and 'topicExpression' must be set")
	public boolean isMutuallyExclusive() {
		return nonNulls(queue, queueExpression, key, keyExpression, topic, topicExpression) == 1;
	}

	private int nonNulls(Object... objects) {
		int result = 0;
		for (Object o : objects) {
			if (o != null) {
				result++;
			}
		}
		return result;
	}
}
