/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.stream.module.kafka.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.kafka.core.Configuration;
import org.springframework.integration.kafka.core.ConnectionFactory;
import org.springframework.integration.kafka.core.DefaultConnectionFactory;
import org.springframework.integration.kafka.core.Partition;
import org.springframework.integration.kafka.core.ZookeeperConfiguration;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.integration.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.integration.kafka.listener.KafkaNativeOffsetManager;
import org.springframework.integration.kafka.listener.OffsetManager;
import org.springframework.integration.kafka.support.ZookeeperConnect;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import kafka.serializer.Decoder;
import kafka.serializer.DefaultDecoder;
import kafka.utils.VerifiableProperties;

/**
 * Source module that receives data from Kafka.
 *
 * @author Soby Chacko
 */
@EnableBinding(Source.class)
@EnableConfigurationProperties(KafkaConfigurationProperties.class)
public class KafkaSource {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int METADATA_VERIFICATION_RETRY_ATTEMPTS = 10;

    private static final double METADATA_VERIFICATION_RETRY_BACKOFF_MULTIPLIER = 2;

    private static final int METADATA_VERIFICATION_RETRY_INITIAL_INTERVAL = 100;

    private static final int METADATA_VERIFICATION_MAX_INTERVAL = 1000;

    @Autowired
    @Bindings(KafkaSource.class)
    private Source source;

    @Autowired
    private KafkaConfigurationProperties properties;

    @Bean
    public KafkaMessageListenerContainer container() {
        Partition[] partitions = getPartitions();

        KafkaMessageListenerContainer container = ObjectUtils.isEmpty(partitions) ?
                new KafkaMessageListenerContainer(kafkaConnectionFactory(), properties.getTopics()) :
                new KafkaMessageListenerContainer(kafkaConnectionFactory(), partitions);

        // if we have less target partitions than target concurrency, adjust accordingly
        container.setConcurrency(Math.min(properties.getConcurrency(), partitions.length));

        container.setMaxFetch(properties.getMaxFetch());
        container.setStopTimeout(properties.getStopTimeout());
        container.setQueueSize(properties.getQueueSize());
        container.setOffsetManager(kafkaNativeOffsetManager(kafkaConnectionFactory()));

        return container;
    }

    @Bean
    public KafkaMessageDrivenChannelAdapter kafkaMessageDrivenChannelAdapter() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        final KafkaMessageDrivenChannelAdapter kafkaMessageDrivenChannelAdapter =
                new KafkaMessageDrivenChannelAdapter(container());
        kafkaMessageDrivenChannelAdapter.setKeyDecoder(getKeyDecoder());
        kafkaMessageDrivenChannelAdapter.setPayloadDecoder(getPayloadDecoder());
        kafkaMessageDrivenChannelAdapter.setOutputChannel(source.output());
        return kafkaMessageDrivenChannelAdapter;
    }

    @Bean
    public ConnectionFactory kafkaConnectionFactory() {
        Configuration zookeeperConfiguration = new ZookeeperConfiguration(zookeeperConnect());
        return new DefaultConnectionFactory(zookeeperConfiguration);
    }

    @Bean
    public ZookeeperConnect zookeeperConnect() {
        ZookeeperConnect zookeeperConnect = new ZookeeperConnect();
        zookeeperConnect.setZkConnect(properties.getZkConnect());
        zookeeperConnect.setZkConnectionTimeout(properties.getZkConnectionTimeout());
        zookeeperConnect.setZkSessionTimeout(properties.getZkSessionTimeout());
        zookeeperConnect.setZkSyncTime(properties.getZkSyncTime());
        return zookeeperConnect;
    }

    @Bean
    public OffsetManager kafkaNativeOffsetManager(ConnectionFactory kafkaConnectionFactory) {
        return new KafkaNativeOffsetManager(kafkaConnectionFactory, zookeeperConnect(),
                getInitialOffsets());
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(METADATA_VERIFICATION_RETRY_ATTEMPTS);
        retryTemplate.setRetryPolicy(simpleRetryPolicy);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(METADATA_VERIFICATION_RETRY_INITIAL_INTERVAL);
        backOffPolicy.setMultiplier(METADATA_VERIFICATION_RETRY_BACKOFF_MULTIPLIER);
        backOffPolicy.setMaxInterval(METADATA_VERIFICATION_MAX_INTERVAL);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    private Decoder<?> getKeyDecoder() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class keyDecoder = properties.getKeyDecoder();
        return getDecoder(keyDecoder);
    }

    private Decoder<?> getPayloadDecoder() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class payloadDecoder = properties.getPayloadDecoder();
        return getDecoder(payloadDecoder);
    }

    private Decoder<?> getDecoder(Class decoder) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (decoder == null) {
            return new DefaultDecoder(new VerifiableProperties());
        }
        return (Decoder) decoder.newInstance();
    }

    private Partition[] getPartitions() {
        Collection<Partition> listenedPartitions = new ArrayList<>();
        if (CollectionUtils.isEmpty(properties.getPartitions())) {
            for (String topic : properties.getTopics()) {
                listenedPartitions.addAll(getPartitionsForTopic(topic));
            }
        }
        else {
            Map<String, String> partitionsMap = properties.getPartitions();

            if (!partitionsMap.isEmpty()) {
                for (Map.Entry<String, String> entry : partitionsMap.entrySet()) {

                    String[] topicPartitions = StringUtils.commaDelimitedListToStringArray(entry.getValue());
                    for (String part : topicPartitions) {
                        listenedPartitions.add(new Partition(entry.getKey(), Integer.valueOf(part)));
                    }
                }
            }
        }
        Partition[] partitions = new Partition[listenedPartitions.size()];
        return listenedPartitions.toArray(partitions);
    }

    private Collection<Partition> getPartitionsForTopic(final String topicName) {
        try {
            return retryTemplate().execute(new RetryCallback<Collection<Partition>, Exception>() {

                @Override
                public Collection<Partition> doWithRetry(RetryContext context) throws Exception {
                    kafkaConnectionFactory().refreshMetadata(Collections.singleton(topicName));
                    Collection<Partition> partitions = kafkaConnectionFactory().getPartitions(topicName);
                    logger.info(String.format("%s - %s : %d", "Number of partitions listening for topic", topicName, partitions.size()));
                    return partitions;
                }
            });
        }
        catch (Exception e) {
            String message = String.format("%s - %s", "Cannot get partition information for topic", topicName);
            logger.error(message, e);
            throw new IllegalStateException(message, e);
        }
    }

    private Map<Partition, Long> getInitialOffsets() {
        Map<Partition, Long> partitionInitOffsets = new HashMap<>();
        Map<String, Map<Integer, Long>> initialOffsets = properties.getInitialOffsets();

        for (Map.Entry<String, Map<Integer, Long>> topicEntry : initialOffsets.entrySet()) {
            for (Map.Entry<Integer, Long> partitionEntry : topicEntry.getValue().entrySet()) {
                Partition partition = new Partition(topicEntry.getKey(), partitionEntry.getKey());
                partitionInitOffsets.put(partition, partitionEntry.getValue());
            }
        }
        return partitionInitOffsets;
    }

}
