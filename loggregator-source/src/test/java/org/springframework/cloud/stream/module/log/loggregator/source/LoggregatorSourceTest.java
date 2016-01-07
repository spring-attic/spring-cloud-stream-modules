package org.springframework.cloud.stream.module.log.loggregator.source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LoggregatorSourceApplication.class)
@EnableConfigurationProperties(LoggregatorProperties.class)
@IntegrationTest({"applicationName=configuration-client",
        "cloudFoundryUser=${cf.user}", "cloudFoundryPassword=${cf.password}", "cloudFoundryApi=${cf.api}"})
@DirtiesContext
public class LoggregatorSourceTest {

    private Log log = LogFactory.getLog(getClass());

    @Autowired
    private Source channels;

    @Autowired
    private MessageCollector messageCollector;

    @Test
    public void testLogReceipt() throws Exception {
        BlockingQueue<Message<?>> messageBlockingQueue =
                this.messageCollector.forChannel(this.channels.output());
        Message<?> message = messageBlockingQueue.poll(1000 * 100, TimeUnit.MILLISECONDS);
        log.info(String.format("received the following log from Loggregator: %s", message.getPayload()));
        assertNotNull(message, "the message can't be null!");
    }
}