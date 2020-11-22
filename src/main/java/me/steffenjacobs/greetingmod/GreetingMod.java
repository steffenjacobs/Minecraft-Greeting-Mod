package me.steffenjacobs.greetingmod;

import me.steffenjacobs.greetingmod.config.ConfigManager;
import me.steffenjacobs.greetingmod.config.GreetingConfiguration;
import me.steffenjacobs.greetingmod.util.LruCache;
import me.steffenjacobs.greetingmod.util.MessageSenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.time.LocalDateTime;

import static me.steffenjacobs.greetingmod.util.MessageSenderUtil.sendRandomMessageForPlayer;

@Mod(value = "greetingmod")
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class GreetingMod {

    private static final LruCache<String, LocalDateTime> USER_LEFT_CACHE = new LruCache<>(16);

    private final GreetingConfiguration configuration;
    private LocalDateTime lastGoodbye = LocalDateTime.now().minusHours(1);

    public GreetingMod() {
        MinecraftForge.EVENT_BUS.register(this);
        configuration = new ConfigManager().load();
    }


    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof PlayerEntity && event.getEntity() == Minecraft.getInstance().player) {
            MessageSenderUtil.sendLocalMessage("[GREETING MOD]: Greeting Mod is active.");
        }
    }

    @SubscribeEvent
    public void chatMessageReceived(ClientChatReceivedEvent event) {
        if (!Minecraft.getInstance().player.getUniqueID().equals(event.getSenderUUID())) {
            ChatMessage message = ChatMessageTokenizer.tokenizeChatMessage(event.getMessage().getString(), configuration);
            if (message.getMessageType() == ChatMessage.MessageType.CHAT && isNotSentByCurrentPlayer(message) && LocalDateTime.now().isAfter(lastGoodbye.plusSeconds(configuration.getGoodbyeCooldownSeconds()))) {
                if (configuration.getGoodbyes().contains(message.getMessage().toLowerCase())) {
                    sendRandomMessageForPlayer(configuration.getGoodbyes(), message.getPlayerName());
                    lastGoodbye = LocalDateTime.now();
                }
            } else if (message.getMessageType() == ChatMessage.MessageType.JOIN && isNotSentByCurrentPlayer(message)) {
                handleJoinMessage(message);
            } else if (message.getMessageType() == ChatMessage.MessageType.LEAVE && isNotSentByCurrentPlayer(message)) {
                USER_LEFT_CACHE.put(message.getPlayerName(), LocalDateTime.now());
            }
        }

    }

    private void handleJoinMessage(ChatMessage message) {
        LocalDateTime leaveTime = USER_LEFT_CACHE.remove(message.getPlayerName());
        if (leaveTime == null || leaveTime.isBefore(LocalDateTime.now().minusSeconds(configuration.getReconnectCooldownSeconds()))) {
            sendRandomMessageForPlayer(configuration.getGreetings(), message.getPlayerName());
        } else if (leaveTime.isBefore(LocalDateTime.now().minusSeconds(configuration.getReconnectWelcomeBackCooldownSeconds()))) {
            sendRandomMessageForPlayer(configuration.getWelcomeBacks(), message.getPlayerName());
        }
    }

    private boolean isNotSentByCurrentPlayer(ChatMessage message) {
        return !Minecraft.getInstance().player.getDisplayName().getString().equals(message.getPlayerName());
    }
}
