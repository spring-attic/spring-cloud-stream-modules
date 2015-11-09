package org.springframework.cloud.stream.module.http;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Configuration properties for the Http Client Processor module.
 *
 * @author Waldemar Hummer
 */
@ConfigurationProperties
public class HttpClientProcessorProperties {

	private static final String DEFAULT_HTTP_METHOD = "GET";

	private static final Expression DEFAULT_RETURN_TYPE = new SpelExpressionParser().parseExpression("''.class");

	private Expression url;

	private String httpMethod = DEFAULT_HTTP_METHOD;

	private Expression body;

	private Expression headers;

	private Expression expectedReturnType = DEFAULT_RETURN_TYPE;

	public Expression getUrl() {
		return url;
	}
	public void setUrl(Expression url) {
		this.url = url;
	}

	public String getHttpMethod() {
		return httpMethod;
	}
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public Expression getBody() {
		return body;
	}
	public void setBody(Expression body) {
		this.body = body;
	}

	public Expression getHeaders() {
		return headers;
	}
	public void setHeaders(Expression headers) {
		this.headers = headers;
	}

	public Expression getExpectedReturnType() {
		return expectedReturnType;
	}
	public void setExpectedReturnType(Expression expectedReturnType) {
		this.expectedReturnType = expectedReturnType;
	}

}
