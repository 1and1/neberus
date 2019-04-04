package net.oneandone.neberus;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import net.oneandone.neberus.annotation.ApiDocumentation;
import net.oneandone.neberus.annotation.ApiUsecase;
import net.oneandone.neberus.annotation.ApiUsecases;
import net.oneandone.neberus.parse.ClassParser;
import net.oneandone.neberus.parse.RestClassData;
import net.oneandone.neberus.parse.RestUsecaseData;
import net.oneandone.neberus.parse.UsecaseParser;
import net.oneandone.neberus.print.DocPrinter;
import net.oneandone.neberus.print.HtmlDocPrinter;
import net.oneandone.neberus.shortcode.ShortCodeExpander;
import net.oneandone.neberus.parse.JavaxWsRsClassParser;
import net.oneandone.neberus.parse.JavaxWsRsMethodParser;
import net.oneandone.neberus.parse.SpringMvcClassParser;
import net.oneandone.neberus.parse.SpringMvcMethodParser;
import net.oneandone.neberus.util.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import javax.ws.rs.Path;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.oneandone.neberus.util.JavaDocUtils.hasAnnotation;

//TODO example response
//TODO accept header in curl
public abstract class Neberus {

    private Neberus() {
    }

    public static boolean start(RootDoc root) {
        System.out.println("Neberus running");

        Options options = Options.parse(root.options());

        ShortCodeExpander expander = new ShortCodeExpander();
        List<NeberusModule> modules = loadModules(expander, options);
        DocPrinter docPrinter = new HtmlDocPrinter(modules, expander, options);

        ClassParser javaxWsRsParser = new JavaxWsRsClassParser(new JavaxWsRsMethodParser(options));
        ClassParser springMvcParser = new SpringMvcClassParser(new SpringMvcMethodParser(options));
        UsecaseParser usecaseParser = new UsecaseParser();

        ClassDoc[] classes = root.classes();

        List<RestClassData> restClasses = new ArrayList<>();
        List<RestUsecaseData> restUsecases = new ArrayList<>();

        String packageDoc = null;

        List<ClassDoc> filteredClasses = Stream.of(classes)
                .filter(classDoc -> options.scanPackages.stream()
                        .anyMatch(pack -> classDoc.containingPackage().name().startsWith(pack)))
                .collect(Collectors.toList());

        for (ClassDoc classDoc : filteredClasses) {
            if (!classDoc.isInterface() && hasAnnotation(classDoc, ApiDocumentation.class)) {

                if (StringUtils.isBlank(packageDoc)) {
                    packageDoc = classDoc.containingPackage().commentText();
                }

                RestClassData restClassData;

                if (usesJavaxWsRs(classDoc)) {
                    restClassData = javaxWsRsParser.parse(classDoc);
                } else {
                    restClassData = springMvcParser.parse(classDoc);
                }

                restClassData.validate(options.ignoreErrors);
                restClasses.add(restClassData);

            }

            modules.forEach(module -> module.parse(classDoc));
        }

        for (ClassDoc classDoc : filteredClasses) {
            if (hasAnnotation(classDoc, ApiUsecase.class) || hasAnnotation(classDoc, ApiUsecases.class)) {
                RestUsecaseData restUsecaseData = usecaseParser.parse(classDoc, restClasses);
                restUsecaseData.validate(options.ignoreErrors);
                restUsecases.add(restUsecaseData);
            }
        }

        restClasses.forEach(restClassData -> {
            docPrinter.printRestClassFile(restClassData, restClasses, restUsecases);
        });

        modules.forEach(NeberusModule::print);

        docPrinter.printIndexFile(restClasses, restUsecases, packageDoc);

        URL bootstrapUrl = Neberus.class.getResource("/static");
        File dest = new File(options.outputDirectory + options.docBasePath);

        System.out.println("Copying static resources");
        FileUtils.copyResourcesRecursively(bootstrapUrl, dest);
        System.out.println("Neberus finished");
        return true;
    }

    private static List<NeberusModule> loadModules(ShortCodeExpander expander, Options options) {
        Reflections reflections = new Reflections();
        Set<Class<? extends NeberusModule>> moduleClasses = reflections.getSubTypesOf(NeberusModule.class);

        if (moduleClasses.isEmpty()) {
            return Collections.emptyList();
        }

        String moduleNames = moduleClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", "));
        System.out.println("Loading modules " + moduleNames);

        return moduleClasses.stream().map(clazz -> {
            try {
                return clazz.getConstructor(ShortCodeExpander.class, Options.class).newInstance(expander, options);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                System.err.println("Can't load module " + clazz.getName() + ": " + e);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static boolean usesJavaxWsRs(ClassDoc classDoc) {
        if (hasAnnotation(classDoc, Path.class)) {
            return true;
        }

        return Stream.of(classDoc.methods()).anyMatch(method -> hasAnnotation(method, Path.class));
    }

    /**
     * Check for doclet-added options. Returns the number of
     * arguments you must specify on the command line for the
     * given option. For example, "-d docs" would return 2.
     * <p>
     * This method is required if the doclet contains any options.
     * If this method is missing, Javadoc will print an invalid flag
     * error for every option.
     *
     * @param option option
     *
     * @return number of arguments on the command line for an option
     * including the option name itself. Zero return means
     * option not known. Negative value means error occurred.
     */
    public static int optionLength(String option) {
        Map<String, Integer> options = new HashMap<>();
        options.put("-d", 2);
        options.put("-docBasePath", 2);
        options.put("-apiVersion", 2);
        options.put("-apiTitle", 2);
        options.put("-apiBasePath", 2);
        options.put("-apiHost", 2);
        options.put("-excludeAnnotationClasses", 2);
        options.put("-ignoreErrors", 1);
        options.put("-scanPackages", 2);

        //FIXME what to do with module options?

        Integer value = options.get(option);
        if (value != null) {
            return value;
        } else {
            return 0;
        }
    }

    /**
     * Return the version of the Java Programming Language supported
     * by this doclet.
     * <p>
     * This method is required by any doclet supporting a language version
     * newer than 1.1.
     *
     * @return the language version supported by this doclet.
     *
     * @since 1.5
     */
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }
}
