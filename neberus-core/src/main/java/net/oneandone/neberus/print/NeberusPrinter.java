package net.oneandone.neberus.print;

import net.oneandone.neberus.Options;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public abstract class NeberusPrinter {

    protected final Options options;

    public NeberusPrinter(Options options) {
        this.options = options;
    }

    protected void saveToFile(String content, String folder, String fileName) {
        try {
            File file = new File(folder);
            if (file.mkdirs()) {
                System.out.println("Created folders for apidocs at " + file.getPath());
            }
            String absoluteFileName = file.getAbsolutePath() + "/" + fileName;
            System.out.println("Writing file '" + absoluteFileName + "'");

            try (PrintWriter printWriter = new PrintWriter(absoluteFileName, StandardCharsets.UTF_8.name())) {
                printWriter.write(content);
            }
        } catch (IOException ex) {
            System.err.println(ex);
            if (!options.ignoreErrors) {
                throw new UncheckedIOException(ex);
            }
        }
    }

}
