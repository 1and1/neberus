package net.oneandone.neberus.print;

import net.oneandone.neberus.NeberusModule;
import net.oneandone.neberus.Options;
import net.oneandone.neberus.parse.RestClassData;
import net.oneandone.neberus.parse.RestUsecaseData;
import net.oneandone.neberus.shortcode.ShortCodeExpander;

import java.util.List;

public abstract class DocPrinter extends NeberusPrinter {

    protected final List<NeberusModule> modules;
    protected final ShortCodeExpander expander;

    public DocPrinter(List<NeberusModule> modules, ShortCodeExpander expander, Options options) {
        super(options);
        this.modules = modules;
        this.expander = expander;
    }

    public abstract void printRestClassFile(RestClassData restClassData, List<RestClassData> allRestClasses,
                                            List<RestUsecaseData> restUsecases);

    public abstract void printIndexFile(List<RestClassData> restClasses, List<RestUsecaseData> restUsecases, String packageDoc);

}
