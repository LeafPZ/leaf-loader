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
package dev.aoqia.leaf.loader.impl.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;

import dev.aoqia.leaf.loader.impl.FormattedException;

public class ModInfo {
    private String name;
    private String id;

    public static @Nullable ModInfo parse(Path modInfo) {
        if (!Files.exists(modInfo)) {
            return null;
        }

        ModInfo info = new ModInfo();

        try (BufferedReader br = Files.newBufferedReader(modInfo)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("name=")) {
                    info.name = line.replace("name=", "");
                } else if (line.startsWith("id=")) {
                    info.id = line.replace("id=", "");
                }
            }
        } catch (IOException e) {
            throw new FormattedException("Failed to parse mod.info for mod at path: %s", modInfo.toString());
        }

        return info;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
