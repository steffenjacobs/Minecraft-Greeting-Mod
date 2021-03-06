package me.steffenjacobs.greetingmod.config;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.regex.Pattern;

@Value
@Builder(toBuilder = true)
public class GreetingConfiguration {
    boolean showErrors;

    List<String> goodbyes;
    List<String> greetings;
    List<String> welcomeBacks;
    List<String> goodbyesLowerCase;
    List<String> greetingsEmoticons;
    List<String> welcomes;

    int goodbyeCooldownSeconds;
    int reconnectCooldownSeconds;
    int reconnectWelcomeBackCooldownSeconds;

    List<Pattern> chatPattern;
    List<Pattern> joinPattern;
    List<Pattern> leavePattern;
    List<Pattern> welcomePattern;
}
