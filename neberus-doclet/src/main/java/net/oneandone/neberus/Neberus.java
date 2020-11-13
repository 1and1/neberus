package net.oneandone.neberus;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import net.oneandone.neberus.annotation.ApiDocumentation;
import net.oneandone.neberus.annotation.ApiUsecase;
import net.oneandone.neberus.annotation.ApiUsecases;
import net.oneandone.neberus.parse.ClassParser;
import net.oneandone.neberus.parse.JavaxWsRsClassParser;
import net.oneandone.neberus.parse.JavaxWsRsMethodParser;
import net.oneandone.neberus.parse.RestClassData;
import net.oneandone.neberus.parse.RestUsecaseData;
import net.oneandone.neberus.parse.SpringMvcClassParser;
import net.oneandone.neberus.parse.SpringMvcMethodParser;
import net.oneandone.neberus.parse.UsecaseParser;
import net.oneandone.neberus.print.DocPrinter;
import net.oneandone.neberus.print.HtmlDocPrinter;
import net.oneandone.neberus.shortcode.ShortCodeExpander;
import net.oneandone.neberus.util.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.ws.rs.Path;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static net.oneandone.neberus.util.JavaDocUtils.getExecutableElements;
import static net.oneandone.neberus.util.JavaDocUtils.getPackageName;
import static net.oneandone.neberus.util.JavaDocUtils.getTypeElements;
import static net.oneandone.neberus.util.JavaDocUtils.hasAnnotation;

//TODO example response
//TODO accept header in curl
public class Neberus implements Doclet {

    private final Options options = new Options();

    @Override
    public boolean run(DocletEnvironment environment) {
        System.out.println("Neberus running");
        options.environment = environment;

        ShortCodeExpander expander = new ShortCodeExpander();
        List<NeberusModule> modules = loadModules(expander, options);
        DocPrinter docPrinter = new HtmlDocPrinter(modules, expander, options);

        ClassParser javaxWsRsParser = new JavaxWsRsClassParser(new JavaxWsRsMethodParser(options));
        ClassParser springMvcParser = new SpringMvcClassParser(new SpringMvcMethodParser(options));
        UsecaseParser usecaseParser = new UsecaseParser(options);

        List<TypeElement> typeElements = getTypeElements(environment);

        List<RestClassData> restClasses = new ArrayList<>();
        List<RestUsecaseData> restUsecases = new ArrayList<>();

        String packageDoc = null;

        List<TypeElement> filteredClasses = typeElements.stream()
                .filter(typeElement -> options.scanPackages.stream()
                        .anyMatch(pack -> getPackageName(typeElement, environment).startsWith(pack)))
                .collect(Collectors.toList());


        for (TypeElement typeElement : filteredClasses) {

            if (!typeElement.getKind().isInterface() && hasAnnotation(typeElement, ApiDocumentation.class, environment)) {
                if (StringUtils.isBlank(packageDoc)) {
                    try {
                        PackageElement packageElement = environment.getElementUtils().getPackageOf(typeElement);
                        FileObject fileForInput = environment.getJavaFileManager().getFileForInput(StandardLocation.SOURCE_PATH, packageElement.getQualifiedName().toString(), "package.html");
                        packageDoc = environment.getDocTrees().getDocCommentTree(fileForInput).toString();
                    } catch (IOException e) {
                        System.err.println(e.toString());
                    }
                }

                RestClassData restClassData;

                if (usesJavaxWsRs(typeElement, options)) {
                    restClassData = javaxWsRsParser.parse(typeElement);
                } else {
                    restClassData = springMvcParser.parse(typeElement);
                }

                restClassData.validate(options.ignoreErrors);
                restClasses.add(restClassData);

            }

            modules.forEach(module -> module.parse(typeElement));
        }

        for (TypeElement typeElement : filteredClasses) {
            if (hasAnnotation(typeElement, ApiUsecase.class, environment)
                    || hasAnnotation(typeElement, ApiUsecases.class, environment)) {
                RestUsecaseData restUsecaseData = usecaseParser.parse(typeElement, restClasses);
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

    private static boolean usesJavaxWsRs(TypeElement typeElement, Options options) {
        if (hasAnnotation(typeElement, Path.class, options.environment)) {
            return true;
        }

        return getExecutableElements(typeElement).stream()
                .anyMatch(method -> hasAnnotation(method, Path.class, options.environment));
    }

    @Override
    public void init(Locale locale, Reporter reporter) {
        // noop
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Set.of(
                // An option that takes no arguments.
                new DocletOption("-ignoreErrors", false, "ignore generation errors", null) {
                    @Override
                    public boolean process(String option, List<String> arguments) {
                        options.ignoreErrors = true;
                        return true;
                    }
                },
                new DocletOption("-d", true, "outputDirectory", "<string>") {
                    @Override
                    public boolean process(String option, List<String> arguments) {
                        options.outputDirectory = arguments.get(0) + "/";
                        return true;
                    }
                },
                new DocletOption("-docBasePath", true, "TODO", "<string>") {
                    @Override
                    public boolean process(String option, List<String> arguments) {
                        options.docBasePath = arguments.get(0);
                        return true;
                    }
                },
                new DocletOption("-apiVersion", true, "TODO", "<string>") {
                    @Override
                    public boolean process(String option, List<String> arguments) {
                        options.apiVersion = arguments.get(0);
                        return true;
                    }
                },
                new DocletOption("-apiTitle", true, "TODO", "<string>") {
                    @Override
                    public boolean process(String option, List<String> arguments) {
                        options.apiTitle = arguments.get(0);
                        return true;
                    }
                },
                new DocletOption("-apiBasePath", true, "TODO", "<string>") {
                    @Override
                    public boolean process(String option, List<String> arguments) {
                        options.apiBasePath = arguments.get(0);
                        return true;
                    }
                },
                new DocletOption("-apiHost", true, "TODO", "<string>") {
                    @Override
                    public boolean process(String option, List<String> arguments) {
                        options.apiHost = arguments.get(0);
                        return true;
                    }
                },
                new DocletOption("-scanPackages", true, "TODO", "<string>") {
                    @Override
                    public boolean process(String option, List<String> arguments) {
                        options.scanPackages = new HashSet<>(Arrays.asList(arguments.get(0).split(";")));
                        return true;
                    }
                });

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_11;
    }

}
