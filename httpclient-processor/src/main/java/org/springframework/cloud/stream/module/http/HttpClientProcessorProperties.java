package org.springframework.cloud.stream.module.http;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Http Client Processor module.
 *
 * @author Waldemar Hummer
 */
@ConfigurationProperties
public class HttpClientProcessorProperties {

	private static final String DEFAULT_HTTP_METHOD = "GET";

	private String url;

	private String httpMethod = DEFAULT_HTTP_METHOD;

	private Object body;

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getHttpMethod() {
		return httpMethod;
	}
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public Object getBody() {
		return body;
	}
	public void setBody(Object body) {
		this.body = body;
	}

}
