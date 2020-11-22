package me.steffenjacobs.greetingmod;


import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatMessage {

    public enum MessageType{
        JOIN, CHAT, LEAVE
    }

    String playerName;
    String message;
    MessageType messageType;
}
