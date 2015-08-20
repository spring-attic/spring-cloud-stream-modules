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

package org.springframework.cloud.stream.module.http;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableModule;
import org.springframework.cloud.stream.annotation.Source;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A source module that listens for http connections as an endpoint and emits the
 * http body as a message payload.
 *
 * @author Eric Bottard
 */
@Controller
@EnableModule(Source.class)
public class HttpSource {

	@Autowired
	private Source channels;

	@RequestMapping(path = "${pathPattern}", method = POST)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void handleRequest(@RequestBody byte[] body) {
		channels.output().send(new GenericMessage<Object>(body));
	}

}
