package com.redispubsubdemo.subscriber;

import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class PrivateSubscriber implements MessageListener {
    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        String messageBody = new String(message.getBody());
        String channel = new String(message.getChannel());

        System.out.println("📢 [PRIVATE] Message: " + messageBody);
        System.out.println("   Channel: " + channel);
        System.out.println("   ---");
    }
}
