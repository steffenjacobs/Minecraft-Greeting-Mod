package me.steffenjacobs.greetingmod.config;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.regex.Pattern;

@Value
@Builder
public class GreetingConfiguration {
    List<String> goodbyes;
    List<String> greetings;
    List<String> welcomeBacks;
    List<String> greetingsEmoticons;

    int goodbyeCooldownSeconds;
    int reconnectCooldownSeconds;
    int reconnectWelcomeBackCooldownSeconds;

    List<Pattern> chatPattern;
    List<Pattern> joinPattern;
    List<Pattern> leavePattern;
}