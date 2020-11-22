package me.steffenjacobs.greetingmod.util;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@UtilityClass
public class MessageSenderUtil {

    private static final UUID EMPTY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final Random random = new Random();
    private static final String MESSAGE_FORMAT = "%s %s";

    public static void sendLocalMessage(String msg) {
        Minecraft.getInstance().player.sendMessage(new StringTextComponent(msg), EMPTY_UUID);
    }

    public static void sendRandomMessageForPlayer(List<String> messages, String playerName) {
        sendRandomMessageForPlayer(messages, playerName, Arrays.asList(""));
    }

    public static void sendRandomMessageForPlayer(List<String> messages, String playerName, List<String> suffixes) {
        String template = getRandomFromList(messages);

        //Lower case template (randomly)
        if (random.nextBoolean()) {
            template = template.toLowerCase();
        }

        //use player name (randomly)
        String innerMessage = !template.contains("%s") ? template : realizeFormatRandomly(playerName, template);

        //use suffix (randomly)
        String fullMessage = realizeFormatRandomly(innerMessage, getRandomFromList(suffixes), MESSAGE_FORMAT);

        Minecraft.getInstance().player.sendChatMessage(fullMessage);
    }

    private static String getRandomFromList(List<String> suffixes) {
        return suffixes.get(random.nextInt(suffixes.size()));
    }

    private static String realizeFormatRandomly(String insert, String template) {
        return String.format(template, random.nextBoolean() ? insert : "");
    }

    private static String realizeFormatRandomly(String insertA, String insertB, String template) {
        return String.format(template, insertA, random.nextBoolean() ? insertB : "");
    }
}
