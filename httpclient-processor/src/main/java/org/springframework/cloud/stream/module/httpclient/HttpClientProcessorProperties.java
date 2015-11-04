/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.module.httpclient;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.http.HttpMethod;

/**
 * Configuration properties for the Http Client Processor module.
 *
 * @author Waldemar Hummer
 * @author Mark Fisher
 */
@ConfigurationProperties
public class HttpClientProcessorProperties {

	private static final HttpMethod DEFAULT_HTTP_METHOD = HttpMethod.GET;

	private static final Class<?> DEFAULT_RETURN_TYPE = String.class;

	private String url;

	private Expression urlExpression;

	private HttpMethod httpMethod = DEFAULT_HTTP_METHOD;

	private Object body;

	private Expression bodyExpression;

	private Expression headersExpression;

	private Class<?> expectedReturnType = DEFAULT_RETURN_TYPE;

	public void setUrl(String url) {
		this.url = url;
	}

	public Expression getUrlExpression() {
		return urlExpression != null ? urlExpression
				: new LiteralExpression(this.url);
	}

	public void setUrlExpression(Expression urlExpression) {
		this.urlExpression = urlExpression;
	}

	@NotNull
	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	public Expression getBodyExpression() {
		return bodyExpression;
	}

	public void setBodyExpression(Expression bodyExpression) {
		this.bodyExpression = bodyExpression;
	}

	public Expression getHeadersExpression() {
		return headersExpression;
	}

	public void setHeadersExpression(Expression headersExpression) {
		this.headersExpression = headersExpression;
	}

	@NotNull
	public Class<?> getExpectedReturnType() {
		return expectedReturnType;
	}

	public void setExpectedReturnType(Class<?> expectedReturnType) {
		this.expectedReturnType = expectedReturnType;
	}

    @AssertTrue(message = "Exactly one of 'url' or 'urlExpression' is required")
    public boolean isExactlyOneUrl() {
    		return url == null ^ urlExpression == null;
    }

    @AssertTrue(message = "At most one of 'body' or 'bodyExpression' is allowed")
    public boolean isAtMostOneBody() {
    		return body == null || bodyExpression == null;
    }
}
