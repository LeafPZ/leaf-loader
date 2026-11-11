/*
 * Copyright (C) 2026 aoqia
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 *
 * This license and file are additionally subjected to the exceptions described in LICENSE_EXCEPTION.md.
 *
 * The exceptions permit independent software to use this project as a dependency under terms of its own choosing, but
 * does not permit the copying or incorporation of covered source code into another work outside the terms of the AGPL.
 */

package dev.aoqia.leaf.loader.impl.util;

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

    @Override
    public JarFile getJarFile() throws IOException {
        connect();
        return jarFile;
    }
}
