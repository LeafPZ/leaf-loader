// spotless:off

/*
 * Copyright 2026 aoqia
 *
 * This file was originally released under LICENSE_UPSTREAM (see below for the original header) and has been relicensed
 * to GNU's AGPLv3 license, with additional terms in the LICENSE_EXCEPTION.md file.
 *
 * All modifications made to this file after the original version were created separate to the original copyright.
 * These modifications are provided under the AGPLv3 license (including exception terms). The portions of this file that
 * were not modified remain subject to the original copyright notice and license, and the original LICENSE is kept in
 * LICENSE_UPSTREAM located alongside LICENSE.
 *
 * The complete record of changes to this file is publicly available in the project's Git history, which anyone may view
 * at any time for any reason.
 *
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// spotless:on

package dev.aoqia.leaf.loader.impl.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipError;
import java.util.zip.ZipFile;

public final class SimpleClassPath implements Closeable {
    private final List<Path> paths;

    /// Whether the path is a jar (otherwise plain dir)
    private final boolean[] jarMarkers;

    private final ZipFile[] openJars;

    public SimpleClassPath(List<Path> paths) {
        this.paths = paths;
        this.jarMarkers = new boolean[paths.size()];
        this.openJars = new ZipFile[paths.size()];

        for (int i = 0; i < jarMarkers.length; i++) {
            if (!Files.isDirectory(paths.get(i))) {
                jarMarkers[i] = true;
            }
        }
    }

    @Override
    public void close() throws IOException {
        IOException exc = null;

        for (int i = 0; i < openJars.length; i++) {
            Closeable file = openJars[i];

            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException e) {
                if (exc == null) {
                    exc = e;
                } else {
                    exc.addSuppressed(e);
                }
            }

            openJars[i] = null;
        }

        if (exc != null) {
            throw exc;
        }
    }

    public CpEntry getEntry(String subPath) throws IOException {
        for (int i = 0; i < jarMarkers.length; i++) {
            if (jarMarkers[i]) {
                ZipFile zf = openJars[i];

                if (zf == null) {
                    Path path = paths.get(i);

                    try {
                        openJars[i] = zf = new ZipFile(path.toFile());
                    } catch (IOException | ZipError e) {
                        throw new IOException(String.format("error opening %s: %s", LoaderUtil.normalizePath(path), e),
                            e);
                    }
                }

                ZipEntry entry = zf.getEntry(subPath);

                if (entry != null) {
                    return new CpEntry(i, subPath, entry);
                }
            } else {
                Path file = paths.get(i).resolve(subPath);

                if (Files.isRegularFile(file)) {
                    return new CpEntry(i, subPath, file);
                }
            }
        }

        return null;
    }

    public InputStream getInputStream(String subPath) throws IOException {
        CpEntry entry = getEntry(subPath);

        return entry != null ? entry.getInputStream() : null;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public final class CpEntry {
        private final int idx;

        private final String subPath;

        private final Object instance;

        private CpEntry(int idx, String subPath, Object instance) {
            this.idx = idx;
            this.subPath = subPath;
            this.instance = instance;
        }

        public InputStream getInputStream() throws IOException {
            if (instance instanceof ZipEntry) {
                return openJars[idx].getInputStream((ZipEntry) instance);
            } else {
                return Files.newInputStream((Path) instance);
            }
        }

        public Path getOrigin() {
            return paths.get(idx);
        }

        public String getSubPath() {
            return subPath;
        }

        @Override
        public String toString() {
            return String.format("%s:%s", getOrigin(), subPath);
        }
    }
}
