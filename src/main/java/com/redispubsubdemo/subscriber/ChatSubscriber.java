package com.redispubsubdemo.subscriber;

import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class ChatSubscriber implements MessageListener {
    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        // Extract message body (as bytes)
        byte[] body = message.getBody();

        // Convert bytes to String
        String messageBody = new String(body);

        // Print to console
        System.out.println("Received message: " + messageBody);

        // Also print channel info
        String channel = new String(message.getChannel());
        System.out.println("From channel: " + channel);

    }
}
