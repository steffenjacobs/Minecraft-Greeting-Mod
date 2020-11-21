package me.steffenjacobs.greetingmod;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ChatMessageTokenizer {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<Pattern> MESSAGE_CHAT_PATTERNS = Arrays.asList(Pattern.compile("<(.*)> (.*)"),
            Pattern.compile("\\[.*\\] (.*) .*> (.*)"));
    private static final List<Pattern> MESSAGE_JOIN_PATTERNS = Arrays.asList(Pattern.compile("(.*) joined the game"),
            Pattern.compile("\\[\\+\\] (.*)"));
    static final UUID SERVER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static ChatMessage tokenizeChatMessage(String fullLine) {
        for (Pattern pattern : MESSAGE_CHAT_PATTERNS) {
            Matcher matcher = pattern.matcher(fullLine);
            if (matcher.find()) {
                return ChatMessage.builder().playerName(matcher.group(1)).message(matcher.group(2)).messageType(ChatMessage.MessageType.CHAT).build();
            }
        }
        for (Pattern pattern : MESSAGE_JOIN_PATTERNS) {
            Matcher matcher = pattern.matcher(fullLine);
            if (matcher.find()) {
                return ChatMessage.builder().playerName(matcher.group(1)).messageType(ChatMessage.MessageType.JOIN).build();
            }
        }
        LOGGER.warn("Could not parse incoming chat message: '{}'", fullLine);
        ((Entity) Minecraft.getInstance().player).sendMessage(new StringTextComponent("[GREETING MOD]: Could not " +
                "parse incoming chat message: '" + fullLine + "'"), SERVER_UUID);
        return ChatMessage.builder().playerName("").message(fullLine).build();
    }
}
