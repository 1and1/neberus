package net.oneandone.neberus.print;

import net.oneandone.neberus.NeberusModule;
import net.oneandone.neberus.Options;
import net.oneandone.neberus.parse.RestClassData;
import net.oneandone.neberus.parse.RestUsecaseData;
import net.oneandone.neberus.shortcode.ShortCodeExpander;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static net.oneandone.neberus.util.JavaDocUtils.asElement;
import static net.oneandone.neberus.util.JavaDocUtils.getDataFields;
import static net.oneandone.neberus.util.JavaDocUtils.getQualifiedName;
import static net.oneandone.neberus.util.JavaDocUtils.isEnum;

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


    protected String concat(String... s) {
        return concat(Arrays.asList(s));
    }

    protected String concat(List<String> strings) {
        StringJoiner sj = new StringJoiner(".");
        strings.stream().filter(Objects::nonNull).filter(StringUtils::isNotEmpty).forEach(sj::add);
        return sj.toString();
    }

    protected boolean isDocumentableSimpleType(TypeMirror type, String fieldName) {
        return fieldName != null
                && asElement(type, options.environment) != null
                && !type.getKind().isPrimitive()
                && !getQualifiedName(type, options.environment).startsWith("java.lang")
                && !isEnum(type, options.environment)
                && !getDataFields(type, options.environment).isEmpty();
    }

}
