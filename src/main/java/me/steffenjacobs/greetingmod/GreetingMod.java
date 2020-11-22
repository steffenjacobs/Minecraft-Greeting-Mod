package me.steffenjacobs.greetingmod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Mod(value = "greetingmod")
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class GreetingMod {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<String> GREETINGS = Arrays.asList("Huhu %s, Hallo %s", "Hi %s", "Hii %s", "Hellu %s",
            "Hi %s :)", "Hiii %s :)", "Hallo %s :)", "Hoi %");
    private static final List<String> GOODBYE = Arrays.asList("Bis dann", "Wiedersehen", "Auf Wiedersehen", "Bye",
            "Byebye", "Goodbye", "Tschuß", "Tschus", "BB",
            "Ciao", "Tschüss", "Bis später", "GN", "Cya",
            "Bis dann :)");
    private static final int GODDBYE_COOLDOWN_SECONDS = 45;

    LocalDateTime lastGoodbye = LocalDateTime.now().minusHours(1);

    public GreetingMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void chatMessageReceived(ClientChatReceivedEvent event) {
        if (!Minecraft.getInstance().player.getUniqueID().equals(event.getSenderUUID())) {
            ChatMessage message = ChatMessageTokenizer.tokenizeChatMessage(event.getMessage().getString());
            if (message.getMessageType() == ChatMessage.MessageType.CHAT && isNotSentByCurrentPlayer(message) && LocalDateTime.now().isAfter(lastGoodbye.plusSeconds(GODDBYE_COOLDOWN_SECONDS))) {
                if (GOODBYE.contains(message.getMessage().toLowerCase())) {
                    sendRandomMessageForPlayer(GOODBYE, message.getPlayerName());
                    lastGoodbye = LocalDateTime.now();
                }
            } else if (message.getMessageType() == ChatMessage.MessageType.JOIN && isNotSentByCurrentPlayer(message)) {
                sendRandomMessageForPlayer(GREETINGS, message.getPlayerName());
            }
        }
    }

    private void sendRandomMessageForPlayer(List<String> messages, String playerName) {
        Random random = new Random();
        String template = messages.get(random.nextInt(messages.size()));
        if (random.nextBoolean()) {
            template = template.toLowerCase();
        }
        Minecraft.getInstance().player.sendChatMessage(String.format(template, random.nextBoolean() ? playerName : ""));
    }

    private boolean isNotSentByCurrentPlayer(ChatMessage message) {
        return !Minecraft.getInstance().player.getDisplayName().getString().equals(message.getPlayerName());
    }
}
