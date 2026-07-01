package com.redispubsubdemo.controller;

import com.redispubsubdemo.config.RedisConfig;
import com.redispubsubdemo.model.ChannelMessage;
import com.redispubsubdemo.model.MessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/publish")
public class PublisherController {

    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/general")
    public String publishGeneral(@RequestBody MessageRequest request) {
        redisTemplate.convertAndSend(RedisConfig.CHANNEL_GENERAL,request.getMessage());
        return "✅ Published to GENERAL:" + request.getMessage();
    }

    @PostMapping("/sports")
    public String publishSports(@RequestBody MessageRequest request) {
        redisTemplate.convertAndSend(RedisConfig.CHANNEL_SPORTS,request.getMessage());
        return "✅ Published to SPORTS:" + request.getMessage();
    }

    @PostMapping("/tech")
    public String publishTech(@RequestBody MessageRequest request) {
        redisTemplate.convertAndSend(RedisConfig.CHANNEL_TECH,request.getMessage());
        return "✅ Published to TECH:" + request.getMessage();
    }
    @PostMapping("/private")
    public String publishPrivate(@RequestBody MessageRequest request) {
        redisTemplate.convertAndSend(RedisConfig.CHANNEL_PRIVATE,request.getMessage());
        return "✅ Published to PRIVATE:" + request.getMessage();
    }

    @PostMapping("/any/{channel}")
    public String publishToAny(@PathVariable String channel, @RequestBody MessageRequest request) {
        String fullChannel = "chat.any."+channel;
        redisTemplate.convertAndSend(fullChannel,request.getMessage());
        return "✅ Published to "+ fullChannel+" : " + request.getMessage();
    }


    @PostMapping("/broadcast")
    public String publishBroadcast(@RequestBody MessageRequest request) {
        String[] channels = {
                RedisConfig.CHANNEL_GENERAL,
                RedisConfig.CHANNEL_PRIVATE,
                RedisConfig.CHANNEL_SPORTS,
                RedisConfig.CHANNEL_TECH
        };
        for (String channel : channels) {
            redisTemplate.convertAndSend(channel, request.getMessage());
        }
        return "✅ Broadcast message sent to ALL channels: " + request.getMessage();
    }

    // Publish with full message object
    @PostMapping("/detailed")
    public String publishDetailed(@RequestBody ChannelMessage message) {
        redisTemplate.convertAndSend("chat." + message.getChannel(),
                "[" + message.getSender() + "] " + message.getMessage());
        return "✅ Detailed message published to " + message.getChannel();
    }

}
