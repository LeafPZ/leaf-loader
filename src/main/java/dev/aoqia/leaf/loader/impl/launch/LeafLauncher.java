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

package dev.aoqia.leaf.loader.impl.launch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.jar.Manifest;

import dev.aoqia.leaf.api.EnvType;

public interface LeafLauncher {
	MappingConfiguration getMappingConfiguration();

	void addToClassPath(Path path, String... allowedPrefixes);
	void setAllowedPrefixes(Path path, String... prefixes);
	void setValidParentClassPath(Collection<Path> paths);

	EnvType getEnvironmentType();

	boolean isClassLoaded(String name);

	/**
	 * Load a class into the game's class loader even if its bytes are only available from the parent class loader.
	 */
	Class<?> loadIntoTarget(String name) throws ClassNotFoundException;

	InputStream getResourceAsStream(String name);

	ClassLoader getTargetClassLoader();

	/**
	 * Gets the byte array for a particular class.
	 *
	 * @param name The name of the class to retrieve
	 * @param runTransformers Whether to run all transformers <i>except mixin</i> on the class
	 */
	byte[] getClassByteArray(String name, boolean runTransformers) throws IOException;

	Manifest getManifest(Path originPath);

	boolean isDevelopment();

	String getEntrypoint();

	List<Path> getClassPath();
}
