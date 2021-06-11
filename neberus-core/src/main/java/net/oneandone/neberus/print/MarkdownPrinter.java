package net.oneandone.neberus.print;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public final class MarkdownPrinter {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownPrinter() {
        parser = Parser.builder().build();
        renderer = HtmlRenderer.builder().build();
    }

    public String print(String rawMarkdown) {
        Node document = parser.parse(rawMarkdown);
        String rendered = renderer.render(document);

        return rendered;
    }
}
