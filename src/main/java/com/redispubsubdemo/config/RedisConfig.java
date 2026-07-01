package com.redispubsubdemo.config;

import com.redispubsubdemo.subscriber.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    public static final String CHANNEL_GENERAL = "chat.general";
    public static final String CHANNEL_SPORTS = "chat.sports";
    public static final String CHANNEL_PRIVATE = "chat.private";
    public static final String CHANNEL_TECH = "chat.tech";

    // 1. Configure RedisTemplate for sending messages
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        JacksonJsonRedisSerializer<Object> jsonRedisSerializer = new JacksonJsonRedisSerializer<>(Object.class);
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);
        return template;
    }

    // 2. Configure Message Listener Container (for receiving messages)
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            ChatSubscriber chatSubscriber,
            GeneralSubscriber generalSubscriber,
            PrivateSubscriber privateSubscriber,
            SportsSubscriber sportsSubscriber,
            TechSubscriber techSubscriber,
            DynamicChannelSubscriber dynamicChannelSubscriber
    ) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Listen to "chat.message" channel
        container.addMessageListener(chatSubscriber, new ChannelTopic("chat.message"));
        container.addMessageListener(generalSubscriber,new ChannelTopic(CHANNEL_GENERAL));
        container.addMessageListener(privateSubscriber, new ChannelTopic(CHANNEL_PRIVATE));
        container.addMessageListener(sportsSubscriber, new ChannelTopic(CHANNEL_SPORTS));
        container.addMessageListener(techSubscriber, new ChannelTopic(CHANNEL_TECH));
        container.addMessageListener(dynamicChannelSubscriber, new PatternTopic("chat.any.*"));

        return container;
    }
}
