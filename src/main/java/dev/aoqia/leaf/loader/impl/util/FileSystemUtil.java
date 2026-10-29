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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipError;

public final class FileSystemUtil {
	public static class FileSystemDelegate implements AutoCloseable {
		private final FileSystem fileSystem;
		private final boolean owner;

		public FileSystemDelegate(FileSystem fileSystem, boolean owner) {
			this.fileSystem = fileSystem;
			this.owner = owner;
		}

		public FileSystem get() {
			return fileSystem;
		}

		@Override
		public void close() throws IOException {
			if (owner) {
				fileSystem.close();
			}
		}
	}

	private FileSystemUtil() { }

	private static final Map<String, String> jfsArgsCreate = Collections.singletonMap("create", "true");
	private static final Map<String, String> jfsArgsEmpty = Collections.emptyMap();

	public static FileSystemDelegate getJarFileSystem(Path path, boolean create) throws IOException {
		return getJarFileSystem(path.toUri(), create);
	}

	public static FileSystemDelegate getJarFileSystem(URI uri, boolean create) throws IOException {
		URI jarUri;

		try {
			jarUri = new URI("jar:" + uri.getScheme(), uri.getHost(), uri.getPath(), uri.getFragment());
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}

		boolean opened = false;
		FileSystem ret = null;

		try {
			ret = FileSystems.getFileSystem(jarUri);
		} catch (FileSystemNotFoundException ignore) {
			try {
				ret = FileSystems.newFileSystem(jarUri, create ? jfsArgsCreate : jfsArgsEmpty);
				opened = true;
			} catch (FileSystemAlreadyExistsException ignore2) {
				ret = FileSystems.getFileSystem(jarUri);
			} catch (IOException | ZipError e) {
				throw new IOException("Error accessing "+uri+": "+e, e);
			}
		}

		return new FileSystemDelegate(ret, opened);
	}
}
