package me.steffenjacobs.greetingmod;

import me.steffenjacobs.greetingmod.config.ConfigManager;
import me.steffenjacobs.greetingmod.config.GreetingConfiguration;
import me.steffenjacobs.greetingmod.util.Scheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GreetingModTest {

    @BeforeAll
    static void setupScheduler() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Scheduler.instance().tick();
            }
        }, 0, 25);
    }

    @Test
    void testInitialJoinMessage() throws NoSuchFieldException, IllegalAccessException {
        //Arrange
        Minecraft minecraftMock = mock(Minecraft.class);
        GreetingMod greetingMod = setupGreetingModForTesting(minecraftMock);

        EntityJoinWorldEvent joinEvent = mock(EntityJoinWorldEvent.class);
        when(joinEvent.getEntity()).thenReturn(minecraftMock.player);

        //Act
        greetingMod.onPlayerJoin(joinEvent);

        //Assert
        verify(minecraftMock.player, Mockito.timeout(5000).times(1)).sendMessage(any(ITextComponent.class),
                any(UUID.class));
    }

    @Test
    void testOwnChatMessageReceived() throws NoSuchFieldException, IllegalAccessException {
        //Arrange
        Minecraft minecraftMock = mock(Minecraft.class);
        GreetingMod greetingMod = setupGreetingModForTesting(minecraftMock);

        ClientChatReceivedEvent chatEvent = mock(ClientChatReceivedEvent.class);
        UUID uniqueID = minecraftMock.player.getUniqueID();
        when(chatEvent.getSenderUUID()).thenReturn(uniqueID);
        ITextComponent messageMock = new StringTextComponent("");
        when(chatEvent.getMessage()).thenReturn(messageMock);

        //Act
        greetingMod.chatMessageReceived(chatEvent);

        //Assert
        verify(minecraftMock.player, Mockito.timeout(3000).times(0)).sendMessage(any(ITextComponent.class),
                any(UUID.class));
    }

    @Test
    void testChatMessageReceivedGoodbye() throws NoSuchFieldException, IllegalAccessException {
        //Arrange
        Minecraft minecraftMock = mock(Minecraft.class);
        GreetingMod greetingMod = setupGreetingModForTesting(minecraftMock);

        ClientChatReceivedEvent chatEvent = createChatEvent(minecraftMock.player.getUniqueID(), "bb");
        ClientChatReceivedEvent chatEvent2 = createChatEvent(minecraftMock.player.getUniqueID(), "bb");
        ClientChatReceivedEvent chatEvent3 = createChatEvent(minecraftMock.player.getUniqueID(), "bb");

        //Act
        greetingMod.chatMessageReceived(chatEvent);
        greetingMod.chatMessageReceived(chatEvent2);
        greetingMod.chatMessageReceived(chatEvent3);

        //Assert
        verify(minecraftMock.player, Mockito.after(3000).never()).sendMessage(any(ITextComponent.class),
                any(UUID.class));
        assertThat(greetingMod.lastGoodbye).isBefore(LocalDateTime.now());
    }

    @Test
    void testChatMessageReceivedJoin() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        //Arrange
        Minecraft minecraftMock = mock(Minecraft.class);
        GreetingMod greetingMod = setupGreetingModForTesting(minecraftMock);

        ClientChatReceivedEvent chatJoinMessage = createChatEvent(UUID.randomUUID(), "Dev2 joined the game.");
        ClientChatReceivedEvent chatLeaveMessage = createChatEvent(UUID.randomUUID(), "Dev2 left the game.");
        ClientChatReceivedEvent chatJoinMessage2 = createChatEvent(UUID.randomUUID(), "Dev2 joined the game.");

        //Act
        greetingMod.chatMessageReceived(chatJoinMessage);
        Thread.sleep(5000);
        greetingMod.chatMessageReceived(chatLeaveMessage);
        greetingMod.chatMessageReceived(chatJoinMessage2);

        //Assert
        verify(minecraftMock.player, Mockito.timeout(5000).times(1)).sendChatMessage(anyString());
    }

    @Test
    void testChatMessageWelcomeBack() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        //Arrange
        Minecraft minecraftMock = mock(Minecraft.class);
        GreetingMod greetingMod = setupGreetingModForTesting(minecraftMock, new ConfigManager().load().toBuilder().reconnectWelcomeBackCooldownSeconds(0).build());

        ClientChatReceivedEvent chatJoinMessage = createChatEvent(UUID.randomUUID(), "Dev2 joined the game.");
        ClientChatReceivedEvent chatLeaveMessage = createChatEvent(UUID.randomUUID(), "Dev2 left the game.");
        ClientChatReceivedEvent chatJoinMessage2 = createChatEvent(UUID.randomUUID(), "Dev2 joined the game.");

        //Act
        greetingMod.chatMessageReceived(chatJoinMessage);
        greetingMod.chatMessageReceived(chatLeaveMessage);
        Thread.sleep(100);
        greetingMod.chatMessageReceived(chatJoinMessage2);

        //Assert
        verify(minecraftMock.player, Mockito.timeout(8000).times(2)).sendChatMessage(anyString());
    }

    @Test
    void testChatMessageReceivedJoinWithZeroDelay() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        //Arrange
        Minecraft minecraftMock = mock(Minecraft.class);
        GreetingMod greetingMod = setupGreetingModForTesting(minecraftMock, new ConfigManager().load().toBuilder().reconnectCooldownSeconds(0).build());

        ClientChatReceivedEvent chatJoinMessage = createChatEvent(UUID.randomUUID(), "Dev2 joined the game.");
        ClientChatReceivedEvent chatLeaveMessage = createChatEvent(UUID.randomUUID(), "Dev2 left the game.");
        ClientChatReceivedEvent chatJoinMessage2 = createChatEvent(UUID.randomUUID(), "Dev2 joined the game.");

        //Act
        greetingMod.chatMessageReceived(chatJoinMessage);
        greetingMod.chatMessageReceived(chatLeaveMessage);
        Thread.sleep(100);
        greetingMod.chatMessageReceived(chatJoinMessage2);

        //Assert
        verify(minecraftMock.player, Mockito.timeout(8000).times(2)).sendChatMessage(anyString());
    }

    private ClientChatReceivedEvent createChatEvent(UUID uid, String message) {
        ClientChatReceivedEvent chatEvent = mock(ClientChatReceivedEvent.class);
        when(chatEvent.getSenderUUID()).thenReturn(uid);
        ITextComponent messageMock = mock(ITextComponent.class);
        when(messageMock.getString()).thenReturn(message);
        when(chatEvent.getMessage()).thenReturn(messageMock);
        return chatEvent;
    }

    private GreetingMod setupGreetingModForTesting(Minecraft minecraftMock, GreetingConfiguration configuration) throws NoSuchFieldException,
            IllegalAccessException {
        setFinalStatic(MinecraftForge.class.getDeclaredField("EVENT_BUS"), mock(IEventBus.class));
        minecraftMock.player = mock(ClientPlayerEntity.class);
        UUID playerId = UUID.randomUUID();
        when(minecraftMock.player.getUniqueID()).thenReturn(playerId);
        when(minecraftMock.player.getDisplayName()).thenReturn(new StringTextComponent("Dev"));
        setFinalStatic(Minecraft.class.getDeclaredField("instance"), minecraftMock);
        return new GreetingMod(configuration);
    }

    private GreetingMod setupGreetingModForTesting(Minecraft minecraftMock) throws NoSuchFieldException,
            IllegalAccessException {
        return setupGreetingModForTesting(minecraftMock, new ConfigManager().load());
    }

    private void setFinalStatic(Field field, Object newValue) throws NoSuchFieldException,
            IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
