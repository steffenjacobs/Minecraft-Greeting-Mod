package me.steffenjacobs.greetingmod;


import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatMessage {

    enum MessageType{
        JOIN, CHAT
    }

    String playerName;
    String message;
    MessageType messageType;
}
