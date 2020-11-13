package net.oneandone.neberus;

import jdk.javadoc.doclet.Doclet;

import java.util.List;

public abstract class DocletOption implements Doclet.Option {

    private final String name;
    private final boolean hasArg;
    private final String description;
    private final String parameters;

    DocletOption(String name, boolean hasArg, String description, String parameters) {
        this.name = name;
        this.hasArg = hasArg;
        this.description = description;
        this.parameters = parameters;
    }

    @Override
    public int getArgumentCount() {
        return hasArg ? 1 : 0;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Kind getKind() {
        return Kind.STANDARD;
    }

    @Override
    public List<String> getNames() {
        return List.of(name);
    }

    @Override
    public String getParameters() {
        return hasArg ? parameters : null;
    }
}
