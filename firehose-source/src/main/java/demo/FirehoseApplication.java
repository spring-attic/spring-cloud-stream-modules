/*
 *
 *  Copyright 2015 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package demo;

import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.tomcat.websocket.WsWebSocketContainer;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.oauth2.OauthClient;
import org.cloudfoundry.client.lib.util.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.integration.websocket.ClientWebSocketContainer;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import source.FirehoseOptionsMetadata;

import javax.net.ssl.SSLContext;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * @author Vinicius Carvalho
 */
@SpringBootApplication
@EnableConfigurationProperties(FirehoseOptionsMetadata.class)
public class FirehoseApplication {

    @Autowired
    private FirehoseOptionsMetadata metadata;

    public static void main(String[] args) {
        SpringApplication.run(FirehoseApplication.class, args);
    }

    @Bean
    public WebSocketClient wsClient(){
        StandardWebSocketClient wsClient = new StandardWebSocketClient();
        if(metadata.isTrustSelfCerts()){
            SSLContext context = buildSslContext();
            wsClient.setUserProperties(Collections.singletonMap(WsWebSocketContainer.SSL_CONTEXT_PROPERTY,context));
        }
        return wsClient;
    }

    @Bean
    public ClientWebSocketContainer webSocketContainer(WebSocketClient client) throws Exception {
        ClientWebSocketContainer container = new ClientWebSocketContainer(client, getDopplerEndpoint());
        HttpHeaders headers = new HttpHeaders();


        if (!StringUtils.isEmpty(metadata.getUsername())) {
            OauthClient oauthClient = new OauthClient(new URL(metadata.getAuthenticationUrl()), new RestUtil().createRestTemplate(null, true));
            oauthClient.init(new CloudCredentials(metadata.getUsername(), metadata.getPassword()));
            headers.add("Authorization", "bearer " + oauthClient.getToken().getValue());
        } else {
            headers.add("Authorization", "");
        }
        container.setHeaders(headers);
        container.setOrigin(metadata.getOrigin());
        return container;
    }



    private SSLContext buildSslContext() {
        try {
            SSLContextBuilder contextBuilder = new SSLContextBuilder().
                    useProtocol("TLS").
                    loadTrustMaterial(null, new TrustSelfSignedStrategy());
            return contextBuilder.build();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }


    private String getDopplerEndpoint() {

        String url = StringUtils.isEmpty(metadata.getDopplerUrl()) ? "wss://doppler." + metadata.getCfDomain() : metadata.getDopplerUrl();
        String subscription = StringUtils.isEmpty(metadata.getDopplerSubscription()) ? "firehose-x" : metadata.getDopplerSubscription();
        return url + "/firehose/" + subscription;
    }
}
