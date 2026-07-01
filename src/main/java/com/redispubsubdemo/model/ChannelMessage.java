package com.redispubsubdemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChannelMessage {
    private String message;
    private String channel;
    private String sender;
    private Instant timestamp;
}
