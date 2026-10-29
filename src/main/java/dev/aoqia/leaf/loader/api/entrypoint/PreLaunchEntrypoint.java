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

package dev.aoqia.leaf.loader.api.entrypoint;

import dev.aoqia.leaf.loader.api.LeafLoader;

/**
 * Entrypoint getting invoked just before launching the game.
 *
 * <p><b>Avoid interfering with the game from this!</b> Accessing anything needs careful consideration to avoid
 * interfering with its own initialization or otherwise harming its state. It is recommended to implement this interface
 * on its own class to avoid running static initializers too early, e.g. because they were referenced in field or method
 * signatures in the same class.
 *
 * <p>The entrypoint is exposed with {@code preLaunch} key in the mod json and runs for any environment. It usually
 * executes several seconds before the {@code main}/{@code client}/{@code server} entrypoints.
 *
 * @see LeafLoader#getEntrypointContainers(String, Class)
 */
@FunctionalInterface
public interface PreLaunchEntrypoint {
	/**
	 * Runs the entrypoint.
	 */
	void onPreLaunch();
}
