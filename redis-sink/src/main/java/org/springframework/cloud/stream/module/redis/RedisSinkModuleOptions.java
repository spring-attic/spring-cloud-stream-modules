package org.springframework.cloud.stream.module.redis;

import java.util.Objects;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Used to configure those Redis Sink module options that are not related to connecting to Redis.
 *
 * @author Eric Bottard
 */
@ConfigurationProperties("module")
public class RedisSinkModuleOptions {

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
		// The 3rd case is needed for conditional enablement in RedisSink
		if (key != null) {
			return new LiteralExpression(key);
		} else if (keyExpression != null) {
			return parser.parseExpression(keyExpression);
		} else {
			return null;
		}
	}

	public void setKeyExpression(String keyExpression) {
		this.keyExpression = keyExpression;
	}

	public Expression getQueueExpression() {
		// The 3rd case is needed for conditional enablement in RedisSink
		if (queue != null) {
			return new LiteralExpression(queue);
		} else if (queueExpression != null) {
			return parser.parseExpression(queueExpression);
		} else {
			return null;
		}
	}

	public void setQueueExpression(String queueExpression) {
		this.queueExpression = queueExpression;
	}

	public Expression getTopicExpression() {
		// The 3rd case is needed for conditional enablement in RedisSink
		if (topic != null) {
			return new LiteralExpression(topic);
		} else if (topicExpression != null) {
			return parser.parseExpression(topicExpression);
		} else {
			return null;
		}
	}

	public void setTopicExpression(String topicExpression) {
		this.topicExpression = topicExpression;
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
