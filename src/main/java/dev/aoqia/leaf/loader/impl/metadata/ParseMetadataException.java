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

package dev.aoqia.leaf.loader.impl.metadata;

import java.util.ArrayList;
import java.util.List;

import dev.aoqia.leaf.loader.impl.lib.gson.JsonReader;

@SuppressWarnings("serial")
public class ParseMetadataException extends Exception {
	private List<String> modPaths;

	public ParseMetadataException(String message) {
		super(message);
	}

	public ParseMetadataException(String message, JsonReader reader) {
		this(message + " Error was located at: " + reader.locationString());
	}

	public ParseMetadataException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public ParseMetadataException(Throwable t) {
		super(t);
	}

	public void setModPaths(String modPath, List<String> modParentPaths) {
		modPaths = new ArrayList<>(modParentPaths);
		modPaths.add(modPath);
	}

	@Override
	public String getMessage() {
		String ret = "Error reading leaf.mod.json file for mod at ";

		if (modPaths == null) {
			ret += "unknown location";
		} else {
			ret += String.join(" -> ", modPaths);
		}

		String msg = super.getMessage();

		if (msg != null) {
			ret += ": "+msg;
		}

		return ret;
	}

	public static class MissingField extends ParseMetadataException {
		public MissingField(String field) {
			super(String.format("Missing required field \"%s\".", field));
		}
	}
}
