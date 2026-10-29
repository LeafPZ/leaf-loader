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

package dev.aoqia.leaf.loader.impl.game.zomboid;

import java.util.OptionalInt;

public final class ZomboidVersion {
	/**
	 * The id from version.json, if available.
	 */
	private final String id;
	private final OptionalInt classVersion;

	private ZomboidVersion(String id, OptionalInt classVersion) {
		this.id = id;
		this.classVersion = classVersion;
	}

	public String getId() {
		return id;
	}

	public OptionalInt getClassVersion() {
		return this.classVersion;
	}

	@Override
	public String toString() {
        return String.format("ZomboidVersion{id=%s, classVersion=%s}", id, classVersion);
	}

	public static final class Builder {
        private String id;
		private OptionalInt classVersion = OptionalInt.empty();

		// Setters
		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setClassVersion(int classVersion) {
			this.classVersion = OptionalInt.of(classVersion);
			return this;
		}

		public Builder setFromFileName(String name) {
			// strip extension
			int pos = name.lastIndexOf('.');
			if (pos > 0) name = name.substring(0, pos);

			return setIdLookup(name);
		}

		public Builder setIdLookup(String version) {
			return setId(ZomboidVersionLookup.getRelease(version));
		}

		public ZomboidVersion build() {
            return new ZomboidVersion(this.id, this.classVersion);
		}
	}
}
