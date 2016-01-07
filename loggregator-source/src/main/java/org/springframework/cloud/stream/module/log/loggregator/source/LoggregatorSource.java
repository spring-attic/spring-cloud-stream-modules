package org.springframework.cloud.stream.module.log.loggregator.source;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * @author <a href="josh@joshlong.com">Josh Long</a>
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties({LoggregatorProperties.class})
public class LoggregatorSource {

    @Autowired
    private LoggregatorProperties loggregatorSourceProperties;

    @Bean
    public LoggregatorMessageSource loggregatorMessageSource(
            @Qualifier(Source.OUTPUT) MessageChannel source,
            CloudFoundryClient cloudFoundryClient) {
        return new LoggregatorMessageSource(
                this.loggregatorSourceProperties.getApplicationName(),
                cloudFoundryClient, source);
    }

    @Bean
    public CloudCredentials cloudCredentials() {
        return new CloudCredentials(this.loggregatorSourceProperties.getCloudFoundryUser(),
                this.loggregatorSourceProperties.getCloudFoundryPassword());
    }

    @Bean
    public CloudFoundryClient cloudFoundryClient(CloudCredentials cc) throws MalformedURLException {
        URI uri = URI.create(this.loggregatorSourceProperties.getCloudFoundryApi());
        CloudFoundryClient cloudFoundryClient = new CloudFoundryClient(cc, uri.toURL());
        cloudFoundryClient.login();
        return cloudFoundryClient;
    }

}

