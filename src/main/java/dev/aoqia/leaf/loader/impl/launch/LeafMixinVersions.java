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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.FabricUtil;

import dev.aoqia.leaf.loader.api.SemanticVersion;
import dev.aoqia.leaf.loader.api.VersionParsingException;

public class LeafMixinVersions {
	private static final List<LoaderMixinVersionEntry> versions = new ArrayList<>();
	private static final Map<Integer, String> minLoaderVersions = new HashMap<>();

	static {
		// maximum loader version and bundled fabric mixin version, DESCENDING ORDER, LATEST FIRST
		// loader versions with new mixin versions need to be added here

		addVersion("0.19.0", FabricUtil.COMPATIBILITY_0_17_1);
		addVersion("0.18.4", FabricUtil.COMPATIBILITY_0_17_0);
		addVersion("0.17.3", FabricUtil.COMPATIBILITY_0_16_5);
		addVersion("0.16.0", FabricUtil.COMPATIBILITY_0_14_0);
		addVersion("0.12.0-", FabricUtil.COMPATIBILITY_0_10_0);
	}

	static List<LoaderMixinVersionEntry> getVersions() {
		return versions;
	}

	public static String getMinLoaderVersion(int mixinCompat) {
		return minLoaderVersions.get(mixinCompat);
	}

	private static void addVersion(String minLoaderVersion, int mixinCompat) {
		try {
			versions.add(new LoaderMixinVersionEntry(SemanticVersion.parse(minLoaderVersion), mixinCompat));
		} catch (VersionParsingException e) {
			throw new RuntimeException(e);
		}

		minLoaderVersions.put(mixinCompat, minLoaderVersion);
	}

	static final class LoaderMixinVersionEntry {
		public final SemanticVersion loaderVersion;
		public final int mixinVersion;

		private LoaderMixinVersionEntry(SemanticVersion loaderVersion, int mixinVersion) {
			this.loaderVersion = loaderVersion;
			this.mixinVersion = mixinVersion;
		}
	}
}
