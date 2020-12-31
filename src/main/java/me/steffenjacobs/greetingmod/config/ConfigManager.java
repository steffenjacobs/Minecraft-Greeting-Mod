package me.steffenjacobs.greetingmod.config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
public class ConfigManager {

    private GreetingConfiguration config;

    @SuppressWarnings("unchecked")
    public GreetingConfiguration load() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("configuration-greeting.yaml");
        Map<String, Object> map = new Yaml().load(inputStream);

        List<String> welcomes = (List<String>) getPathTwo("welcome.messages", map);
        welcomes.addAll((List<String>) getPathTwo("greeting.messages", map));

        config = GreetingConfiguration.builder()
                .goodbyeCooldownSeconds((Integer) getPathThree("goodbye.cooldown.seconds", map))
                .goodbyes((List<String>) getPathTwo("goodbye.messages", map))
                .goodbyesLowerCase(((List<String>) getPathTwo("goodbye.messages", map)).stream().map(String::toLowerCase).collect(Collectors.toList()))
                .greetings((List<String>) getPathTwo("greeting.messages", map))
                .greetingsEmoticons((List<String>) getPathTwo("greeting.emoticons", map))
                .reconnectCooldownSeconds((Integer) getPathThree("reconnect.cooldown.seconds", map))
                .welcomes(welcomes)
                .reconnectWelcomeBackCooldownSeconds((Integer) getPathThree("reconnect.cooldown.welcomeback_seconds",
                        map))
                .welcomeBacks((List<String>) getPathTwo("reconnect.messages", map))
                .chatPattern(createPatterns(getPathTwo("pattern.chat", map)))
                .joinPattern(createPatterns(getPathTwo("pattern.join", map)))
                .leavePattern(createPatterns(getPathTwo("pattern.leave", map)))
                .welcomePattern(createPatterns(getPathTwo("pattern.welcome", map)))
                .showErrors((Boolean) map.get("show-errors"))
                .build();

        return config;
    }

    @SuppressWarnings("unchecked")
    private Object getPathTwo(String path, Map<String, Object> map) {
        String[] keys = path.split("\\.");
        return ((Map<String, Object>) map.get(keys[0])).get(keys[1]);
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    private List<Pattern> createPatterns(Object list) {
        return ((List<String>) list).stream().map(Pattern::compile).collect(Collectors.toList());
    }
}
