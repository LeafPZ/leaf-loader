package dev.aoqia.leaf.loader.impl.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@SuppressWarnings("FieldCanBeLocal")
public class UnverifiedJarURLConnection extends JarURLConnection {
    private JarFile jarFile;
    private JarEntry jarEntry;

    public UnverifiedJarURLConnection(URL url) throws IOException {
        super(url);
    }

    @Override
    public JarFile getJarFile() throws IOException {
        connect();
        return jarFile;
    }

    @Override
    public void connect() throws IOException {
        if (connected) {
            return;
        }

        jarFileURLConnection = getJarFileURL().openConnection();
        jarFileURLConnection.setUseCaches(useCaches);

        jarFile = new JarFile(getJarFileURL().getFile(), false);

        String entryName = getEntryName();

        if (entryName != null) {
            jarEntry = (JarEntry) jarFile.getEntry(entryName);

            if (jarEntry == null) {
                try {
                    jarFile.close();
                } catch (Exception ignored) {
                }

                throw new FileNotFoundException(
                    String.format("JAR entry %s not found in jar file %s", entryName, jarFile.getName()));
            }
        }

        connected = true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();

        String entryName = getEntryName();
        if (entryName == null) {
            throw new IOException("no entry name specified");
        }

        if (jarEntry == null) {
            throw new FileNotFoundException(
                String.format("JAR entry %s not found in jar file %s", entryName, jarFile.getName()));
        }

        return jarFile.getInputStream(jarEntry);
    }
}
