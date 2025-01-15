package com.example.cdcconsumer.global.infra.kafka;

import com.example.cdcconsumer.domain.interaction.model.NewInteractionCaptureEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Value("${custom.kafka-url}")
    private String kafkaURL;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaURL);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);  // Value는 JSON으로 직렬화

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    private ConsumerFactory<String, Object> createConsumerFactory(String groupId) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaURL);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    private ConcurrentKafkaListenerContainerFactory<String, Object> createKafkaListenerContainerFactory(ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setRecordMessageConverter(new StringJsonMessageConverter());
        return factory;
    }

    // Comment Group
    @Bean
    public ConsumerFactory<String, Object> commentConsumerFactory() {
        return createConsumerFactory("comment_payload_group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> commentKafkaListenerContainerFactory() {
        return createKafkaListenerContainerFactory(commentConsumerFactory());
    }

    // User Group
    @Bean
    public ConsumerFactory<String, Object> userConsumerFactory() {
        return createConsumerFactory("user_payload_group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> userKafkaListenerContainerFactory() {
        return createKafkaListenerContainerFactory(userConsumerFactory());
    }

    // Emoji Group
    @Bean
    public ConsumerFactory<String, Object> emojiConsumerFactory() {
        return createConsumerFactory("emoji_payload_group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> emojiKafkaListenerContainerFactory() {
        return createKafkaListenerContainerFactory(emojiConsumerFactory());
    }

    // Role Group
    @Bean
    public ConsumerFactory<String, Object> roleConsumerFactory() {
        return createConsumerFactory("role_payload_group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> roleKafkaListenerContainerFactory() {
        return createKafkaListenerContainerFactory(roleConsumerFactory());
    }

    // Interaction Group
    @Bean
    public ConsumerFactory<String, Object> interactionConsumerFactory() {
        return createConsumerFactory("interaction_payload_group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> interactionKafkaListenerContainerFactory() {
        return createKafkaListenerContainerFactory(interactionConsumerFactory());
    }

    // Post Group
    @Bean
    public ConsumerFactory<String, Object> postConsumerFactory() {
        return createConsumerFactory("post_payload_group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> postKafkaListenerContainerFactory() {
        return createKafkaListenerContainerFactory(postConsumerFactory());
    }
}