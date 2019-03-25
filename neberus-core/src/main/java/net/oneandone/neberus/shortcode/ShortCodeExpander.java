package net.oneandone.neberus.shortcode;

import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ShortCodeExpander {

    private final Map<String, Function<String, String>> shortCodes;
    private final Pattern shortCodePattern = Pattern.compile("\\[\\[[^\\[]*]\\[[^\\[]*]]");

    public ShortCodeExpander() {
        shortCodes = loadShortCodes();
    }

    private Map<String, Function<String, String>> loadShortCodes() {
        Reflections reflections = new Reflections();
        Set<Class<? extends ShortCode>> shortcodeClasses = reflections.getSubTypesOf(ShortCode.class);

        if (shortcodeClasses.isEmpty()) {
            return Collections.emptyMap();
        }

        String shortCodeNames = shortcodeClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", "));
        System.out.println("Loading shortcodes " + shortCodeNames);

        Map<String, Function<String, String>> loadedShortCodes = new HashMap<>();

        shortcodeClasses.forEach(clazz -> {
            try {
                ShortCode shortCodeInstance = clazz.getConstructor().newInstance();
                loadedShortCodes.put(shortCodeInstance.getKey(), shortCodeInstance::process);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                System.err.println("Can't load shortcode " + clazz.getName() + ": " + e);
            }
        });

        return loadedShortCodes;
    }

    public String expand(String input) {
        Map<String, String> matches = new HashMap<>();

        Matcher matcher = shortCodePattern.matcher(input);

        while (matcher.find()) {
            String match = matcher.group();

            String key = match.replaceFirst("\\[\\[(.*)]\\[.*", "$1");
            String value = match.replaceFirst(".*]\\[(.*)]].*", "$1");

            if (shortCodes.containsKey(key)) {
                String expanded = shortCodes.get(key).apply(value);
                matches.put(match, expanded);
            } else {
                System.err.println("Unknown shortcode key: " + key);
            }
        }

        String output = input;

        for (Map.Entry<String, String> match : matches.entrySet()) {
            output = output.replace(match.getKey(), match.getValue());
        }

        return output;
    }

}
