package me.steffenjacobs.greetingmod;

import lombok.experimental.UtilityClass;
import me.steffenjacobs.greetingmod.config.GreetingConfiguration;
import me.steffenjacobs.greetingmod.util.MessageSenderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ChatMessageTokenizer {

    private static final Logger LOGGER = LogManager.getLogger();

    public static ChatMessage tokenizeChatMessage(String fullLine, GreetingConfiguration config) {
        return match(fullLine, config.getChatPattern(), ChatMessage.MessageType.CHAT).orElseGet(() ->
                match(fullLine, config.getJoinPattern(), ChatMessage.MessageType.JOIN).orElseGet(() ->
                        match(fullLine, config.getLeavePattern(), ChatMessage.MessageType.LEAVE).orElseGet(() -> createErrorMessage(fullLine, config))));
    }

    private static ChatMessage createErrorMessage(String fullLine, GreetingConfiguration configuration) {
        LOGGER.warn("Could not parse incoming chat message: '{}'", fullLine);
        if (configuration.isShowErrors()) {
            MessageSenderUtil.sendLocalMessage(String.format("[GREETING MOD]: Could not parse incoming chat message: " +
                    "'%s'", fullLine));
        }
        return ChatMessage.builder().playerName("").message(fullLine).build();
    }

    private Optional<ChatMessage> match(String fullLine, List<Pattern> patterns, ChatMessage.MessageType messageType) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(fullLine);
            if (matcher.find()) {
                return Optional.of(ChatMessage.builder()
                        .playerName(matcher.group(1))
                        .message(matcher.groupCount() > 1 ? matcher.group(2) : fullLine)
                        .messageType(messageType)
                        .build());
            }
        }
        return Optional.empty();
    }
}
