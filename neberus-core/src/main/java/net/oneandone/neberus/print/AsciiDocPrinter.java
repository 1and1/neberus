package net.oneandone.neberus.print;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;

public final class AsciiDocPrinter {

    private final Asciidoctor asciidoctor;
    private final Options options;

    public AsciiDocPrinter() {
        asciidoctor = Asciidoctor.Factory.create();
        options = Options.builder().build();
    }

    public String print(String rawAsciidoc) {
        String strippedLeadingSpace = rawAsciidoc.replaceAll("\n ", "\n").stripLeading();

        return asciidoctor.convert(strippedLeadingSpace, options);
    }

}
