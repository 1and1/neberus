package net.oneandone.neberus;

import com.sun.javadoc.ClassDoc;
import net.oneandone.neberus.shortcode.ShortCodeExpander;

public abstract class NeberusModule {

    protected final Options options;
    protected final ShortCodeExpander expander;

    protected NeberusModule(ShortCodeExpander expander, Options options) {
        this.expander = expander;
        this.options = options;
    }

    public abstract void parse(ClassDoc classDoc);

    public abstract void print();

    public abstract String getName();

    public abstract String getFilename();
}
