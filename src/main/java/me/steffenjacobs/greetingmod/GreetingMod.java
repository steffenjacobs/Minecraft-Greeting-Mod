package me.steffenjacobs.greetingmod;

import me.steffenjacobs.greetingmod.config.ConfigManager;
import me.steffenjacobs.greetingmod.config.GreetingConfiguration;
import me.steffenjacobs.greetingmod.util.LruCache;
import me.steffenjacobs.greetingmod.util.MessageSenderUtil;
import me.steffenjacobs.greetingmod.util.Scheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static me.steffenjacobs.greetingmod.util.MessageSenderUtil.sendRandomMessageForPlayer;

@Mod(value = "greetingmod")
@Mod.EventBusSubscriber(value = Dist.CLIENT)

public class GreetingMod {

    private static final Map<String, LocalDateTime> USER_LEFT_CACHE = Collections.synchronizedMap(new LruCache<>(16));

    private final GreetingConfiguration configuration;
    LocalDateTime lastGoodbye = LocalDateTime.now().minusHours(1);
    private final List<Predicate<ChatMessage>> messageStrategies;


    public GreetingMod() {
        this(new ConfigManager().load());
    }

    public GreetingMod(GreetingConfiguration configuration) {
        this.configuration = configuration;
        messageStrategies = Arrays.asList(this::handleGoodbyeMessage, this::handleJoinMessage,
                this::handleWelcomeMessage, this::handleLeaveMessage);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        Scheduler.instance().tick();
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
            ChatMessage message = ChatMessageTokenizer.tokenizeChatMessage(event.getMessage().getString(),
                    configuration);

            for (Predicate<ChatMessage> strategy : messageStrategies) {
                if (strategy.test(message)) {
                    break;
                }
            }
        } else {
            //Check if current player said goodbye -> Avoid second message on answers from other players
            ChatMessage message = ChatMessageTokenizer.tokenizeChatMessage(event.getMessage().getString(),
                    configuration);
            if (configuration.getGoodbyesLowerCase().contains(message.getMessage().toLowerCase())) {
                lastGoodbye = LocalDateTime.now();
            }
        }

    }

    /**
     * e.g. 'Dev left.'
     * <p>
     * Given: Message was identified as LEAVE message AND message was not sent by current player
     * Then: add user to leave-cache
     */
    private boolean handleLeaveMessage(ChatMessage message) {
        if (message.getMessageType() == ChatMessage.MessageType.LEAVE && isNotSentByCurrentPlayer(message)) {
            USER_LEFT_CACHE.put(message.getPlayerName(), LocalDateTime.now());
            return true;
        }
        return false;
    }

    /**
     * e.g. '[Server]: We welcome Dev to our server.'
     * <p>
     * Given: Message was identified as WELCOME message AND message was not sent by current player
     * Then: say hello
     */
    private boolean handleWelcomeMessage(ChatMessage message) {
        if (message.getMessageType() == ChatMessage.MessageType.WELCOME && isNotSentByCurrentPlayer(message)) {
            sendRandomMessageForPlayer(configuration.getWelcomes(), message.getPlayerName(),
                    configuration.getGreetingsEmoticons(), USER_LEFT_CACHE::containsKey, true);
            return true;
        }
        return false;
    }

    /**
     * e.g. 'Byebye'
     * <p>
     * Given: Message was identified as CHAT message AND message was not sent by current player AND there was not
     * just a goodbye message in chat
     * Case: Message can be identified as goodbye message: say goodbye as well
     */
    private boolean handleGoodbyeMessage(ChatMessage message) {
        if (message.getMessageType() == ChatMessage.MessageType.CHAT
                && isNotSentByCurrentPlayer(message)
                && LocalDateTime.now().isAfter(lastGoodbye.plusSeconds(configuration.getGoodbyeCooldownSeconds()))
                && configuration.getGoodbyesLowerCase().contains(message.getMessage().toLowerCase())) {
            sendRandomMessageForPlayer(configuration.getGoodbyes(), message.getPlayerName(),
                    configuration.getGreetingsEmoticons());
            lastGoodbye = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * e.g. 'Dev joined the game.'
     * <p>
     * Given: Message was identified as JOIN message AND message was not sent by current player
     * Case 1: Involved player was not found in leave-cache or reconnect cooldown expired: greet player
     * Case 2: Involved player left and reconnect cooldown expired but welcome back cooldown did not expire yet:
     * welcome-back player
     */
    private boolean handleJoinMessage(ChatMessage message) {
        if (message.getMessageType() == ChatMessage.MessageType.JOIN && isNotSentByCurrentPlayer(message)) {
            LocalDateTime leaveTime = USER_LEFT_CACHE.remove(message.getPlayerName());
            if (leaveTime == null || leaveTime.isBefore(LocalDateTime.now().minusSeconds(configuration.getReconnectCooldownSeconds()))) {
                sendRandomMessageForPlayer(configuration.getGreetings(), message.getPlayerName(),
                        configuration.getGreetingsEmoticons(), USER_LEFT_CACHE::containsKey, true);
                return true;
            } else if (leaveTime.isBefore(LocalDateTime.now().minusSeconds(configuration.getReconnectWelcomeBackCooldownSeconds()))) {
                sendRandomMessageForPlayer(configuration.getWelcomeBacks(), message.getPlayerName(),
                        configuration.getGreetingsEmoticons(), USER_LEFT_CACHE::containsKey, true);
                return true;
            }
        }
        return false;
    }

    private boolean isNotSentByCurrentPlayer(ChatMessage message) {
        return !Minecraft.getInstance().player.getDisplayName().getString().equals(message.getPlayerName());
    }
}
