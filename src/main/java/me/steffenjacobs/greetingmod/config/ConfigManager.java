package me.steffenjacobs.greetingmod.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigManager {

    private GreetingConfiguration config;

    public GreetingConfiguration load() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("configuration.yaml");
        Map<String, Object> map = new Yaml().load(inputStream);

        config = GreetingConfiguration.builder()
                .goodbyeCooldownSeconds((Integer) getPathThree("goodbye.cooldown.seconds", map))
                .goodbyes((List<String>) getPathTwo("goodbye.messages", map))
                .greetings((List<String>) getPathTwo("greeting.messages", map))
                .greetingsEmoticons((List<String>) getPathTwo("greeting.emoticons", map))
                .reconnectCooldownSeconds((Integer) getPathThree("reconnect.cooldown.seconds", map))
                .reconnectWelcomeBackCooldownSeconds((Integer) getPathThree("reconnect.cooldown.welcomeback_seconds",
                        map))
                .welcomeBacks((List<String>) getPathTwo("reconnect.messages", map))
                .chatPattern(createPatterns(getPathTwo("pattern.chat", map)))
                .joinPattern(createPatterns(getPathTwo("pattern.join", map)))
                .leavePattern(createPatterns(getPathTwo("pattern.leave", map)))
                .build();

        return config;
    }

    private Object getPathTwo(String path, Map<String, Object> map) {
        String[] keys = path.split("\\.");
        return ((Map<String, Object>) map.get(keys[0])).get(keys[1]);
    }

    private Object getPathThree(String path, Map<String, Object> map) {
        String[] keys = path.split("\\.");
        return ((Map<String, Object>) ((Map<String, Object>) map.get(keys[0])).get(keys[1])).get(keys[2]);
    }

    public GreetingConfiguration getConfiguration() {
        if (config == null) {
            load();
        }
        return config;
    }

    private List<Pattern> createPatterns(Object list) {
        return ((List<String>) list).stream().map(Pattern::compile).collect(Collectors.toList());
    }
}
