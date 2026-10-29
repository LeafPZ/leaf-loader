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

package dev.aoqia.leaf.loader.impl.discovery;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import dev.aoqia.leaf.loader.api.SemanticVersion;
import dev.aoqia.leaf.loader.api.VersionParsingException;
import dev.aoqia.leaf.loader.impl.LeafLoaderImpl;

public class WorkshopModCandidateFinder extends CachedirModCandidateFinder {
    private final Path workshopDir;

    public WorkshopModCandidateFinder(Path workshopDir, SemanticVersion gameVersion, boolean requiresRemap) {
        super(workshopDir, gameVersion, requiresRemap);

        this.workshopDir = workshopDir;
    }

    @Override
    public void findCandidates(ModCandidateConsumer out) {
        if (!Files.exists(this.workshopDir)) {
            return;
        }

        if (!Files.isDirectory(this.workshopDir)) {
            throw new RuntimeException(this.workshopDir + " is not a directory!");
        }

        try {
            // Workshop folder
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.workshopDir, Files::isDirectory)) {
                for (Path rootModFolder : stream) {
                    // If the folder matches the loader mod's internal id, don't discover it
                    // This needs to be done in every candidate finder to prevent problems later on
                    if (LeafLoaderImpl.WORKSHOP_ID.equalsIgnoreCase(rootModFolder.getFileName().toString())) {
                        continue;
                    }

                    Path modsFolder = rootModFolder.resolve("mods");
                    if (!Files.exists(modsFolder) || !Files.isDirectory(modsFolder)) {
                        continue;
                    }

                    try (DirectoryStream<Path> stream2 = Files.newDirectoryStream(modsFolder, Files::isDirectory)) {
                        findModsInModRoot(out, stream2, ModSource.WORKSHOP);
                    }
                }
            }
        } catch (IOException | VersionParsingException e) {
            throw new RuntimeException("Exception while searching for mods in workshopDir!", e);
        }
    }
}
