package net.oneandone.neberus.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

public abstract class FileUtils {

    private FileUtils() {
    }

    public static boolean copyFile(final File toCopy, final File destFile) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(toCopy);
            fileOutputStream = new FileOutputStream(destFile);
            return FileUtils.copyStream(fileInputStream, fileOutputStream);
        } catch (final FileNotFoundException e) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            tryClose(fileInputStream);
            tryClose(fileOutputStream);
        }
        return false;
    }

    private static void tryClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ex) {
                Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static boolean copyFilesRecusively(final File toCopy,
                                               final File destDir) {
        assert destDir.isDirectory();

        if (!toCopy.isDirectory()) {
            return FileUtils.copyFile(toCopy, new File(destDir, toCopy.getName()));
        } else {
            final File newDestDir = new File(destDir, toCopy.getName());
            if (!newDestDir.exists() && !newDestDir.mkdir()) {
                return false;
            }
            File[] listFiles = toCopy.listFiles();
            if (listFiles != null) {
                for (final File child : listFiles) {
                    if (!FileUtils.copyFilesRecusively(child, newDestDir)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean copyJarResourcesRecursively(final File destDir,
                                                      final JarURLConnection jarConnection) throws IOException {

        final JarFile jarFile = jarConnection.getJarFile();

        for (final Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
            final JarEntry entry = e.nextElement();
            if (entry.getName().startsWith(jarConnection.getEntryName())) {
                final String filename = StringUtils.removeStart(entry.getName(), //
                        jarConnection.getEntryName());

                final File f = new File(destDir, filename);
                if (!entry.isDirectory()) {
                    try (InputStream entryInputStream = jarFile.getInputStream(entry)) {
                        if (!FileUtils.copyStream(entryInputStream, f)) {
                            return false;
                        }
                    }
                } else if (!FileUtils.ensureDirectoryExists(f)) {
                    throw new IOException("Could not create directory: " +
                            f.getAbsolutePath());
                }
            }
        }
        return true;
    }

    public static boolean copyResourcesRecursively(final URL originUrl, final File destination) {
        try {
            final URLConnection urlConnection = originUrl.openConnection();
            if (urlConnection instanceof JarURLConnection) {
                return FileUtils.copyJarResourcesRecursively(destination,
                        (JarURLConnection) urlConnection);
            } else {
                return FileUtils.copyFilesRecusively(new File(originUrl.getPath()),
                        destination);
            }
        } catch (final IOException e) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    public static List<String> listFiles(URL originUrl) {

        List<String> files = new ArrayList<>();

        if (originUrl == null) {
            return files;
        }

        final URLConnection urlConnection;
        try {
            urlConnection = originUrl.openConnection();

            if (urlConnection instanceof JarURLConnection) {
                Enumeration<JarEntry> entries = ((JarURLConnection) urlConnection).getJarFile().entries();

                while (entries.hasMoreElements()) {
                    files.add(entries.nextElement().getName());
                }

            } else {
                files.addAll(Arrays.asList(new File(originUrl.getPath()).list()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }

    private static boolean copyStream(final InputStream is, final File f) {
        try {
            return FileUtils.copyStream(is, new FileOutputStream(f));
        } catch (final FileNotFoundException e) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    private static boolean copyStream(final InputStream is, final OutputStream os) {
        try {
            final byte[] buf = new byte[1024];

            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            is.close();
            os.close();
            return true;
        } catch (final IOException e) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    private static boolean ensureDirectoryExists(final File f) {
        return f.exists() || f.mkdir();
    }
}
