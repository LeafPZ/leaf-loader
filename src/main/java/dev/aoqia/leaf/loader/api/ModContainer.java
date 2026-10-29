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

package dev.aoqia.leaf.loader.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import dev.aoqia.leaf.loader.api.metadata.ModMetadata;
import dev.aoqia.leaf.loader.api.metadata.ModOrigin;

/**
 * Represents a mod.
 */
public interface ModContainer {
	/**
	 * Returns the metadata of this mod.
	 */
	ModMetadata getMetadata();

	/**
	 * Returns the root directories of the mod (inside JAR/folder), exposing its contents.
	 *
	 * <p>The paths may point to regular folders or into mod JARs. Multiple root paths may occur in development
	 * environments with {@code -Dfabric.classPathGroups} as used in multi-project mod setups.
	 *
	 * <p>A path returned by this method may be incompatible with {@link Path#toFile} as its FileSystem doesn't
	 * necessarily represent the OS file system, but potentially a virtual view of jar contents or another abstraction.
	 *
	 * @return the root directories of the mod, may be empty for builtin or other synthetic mods
	 */
	List<Path> getRootPaths();

	/**
	 * Gets an NIO reference to a file inside the JAR/folder.
	 *
	 * <p>The path, if present, is guaranteed to exist!
	 *
	 * <p>A path returned by this method may be incompatible with {@link Path#toFile} as its FileSystem doesn't
	 * necessarily represent the OS file system, but potentially a virtual view of jar contents or another abstraction.
	 *
	 * @param file The location from a root path, using {@code /} as a separator.
	 * @return optional containing the path to a given file or empty if it can't be found
	 */
	default Optional<Path> findPath(String file) {
		for (Path root : getRootPaths()) {
			Path path = root.resolve(file.replace("/", root.getFileSystem().getSeparator()));
			if (Files.exists(path)) return Optional.of(path);
		}

		return Optional.empty();
	}

	/**
	 * Gets where the mod was loaded from originally, the mod jar/folder itself.
	 *
	 * <p>This location is not necessarily identical to the code source used at runtime, a mod may get copied or
	 * otherwise transformed before being put on the class path. It thus mostly represents the installation and initial
	 * loading, not what is being directly accessed at runtime.
	 *
	 * <p>The mod origin is provided for working with the installation like telling the user where a mod has been
	 * installed at. Accessing the files inside a mod jar/folder should use {@link #findPath} and {@link #getRootPaths}
	 * instead. Those also abstract jar accesses through the virtual {@code ZipFileSystem} away.
	 *
	 * @return mod origin
	 */
	ModOrigin getOrigin();

	/**
	 * Get the mod containing this mod (nested jar parent).
	 *
	 * @return mod containing this mod or empty if not nested
	 */
	Optional<ModContainer> getContainingMod();

	/**
	 * Get the active mods contained within this mod (nested jar children).
	 *
	 * @return active contained mods within this mod's jar
	 */
	Collection<ModContainer> getContainedMods();
}
