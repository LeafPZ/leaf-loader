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

package dev.aoqia.leaf.loader.impl.discovery;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import dev.aoqia.leaf.loader.impl.util.LoaderUtil;
import dev.aoqia.leaf.loader.impl.util.log.Log;
import dev.aoqia.leaf.loader.impl.util.log.LogCategory;

public class DirectoryModCandidateFinder implements ModCandidateFinder {
    private final Path path;
    private final boolean requiresRemap;
    private final int depth;
    private final @Nullable Path subpath;

    public DirectoryModCandidateFinder(Path path, boolean requiresRemap, int depth) {
        this.path = LoaderUtil.normalizePath(path);
        this.requiresRemap = requiresRemap;
        this.depth = depth;
        this.subpath = null;
    }

    public DirectoryModCandidateFinder(Path path, boolean requiresRemap, int depth, @Nullable Path subpath) {
        this.path = LoaderUtil.normalizePath(path);
        this.requiresRemap = requiresRemap;
        this.depth = depth;
        this.subpath = subpath;
    }

    static boolean isValidFile(Path path) {
        /*
         * We only propose a file as a possible mod in the following scenarios:
         * General: Must be a jar file
         *
         * Some OSes Generate metadata so consider the following because of OSes:
         * UNIX: Exclude if file is hidden; this occurs when starting a file name with `.`
         * MacOS: Exclude hidden + startsWith "." since Mac OS names their metadata files in the form of `.mod.jar`
         */

        if (!Files.isRegularFile(path)) {
            return false;
        }

        try {
            if (Files.isHidden(path)) {
                return false;
            }
        } catch (IOException e) {
            Log.warn(LogCategory.DISCOVERY, "Error checking if file %s is hidden", path, e);
            return false;
        }

        String fileName = path.getFileName().toString();

        return fileName.endsWith(".jar") && !fileName.startsWith(".");
    }

    @Override
    public void findCandidates(ModCandidateConsumer out) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
                return;
            } catch (IOException e) {
                throw new RuntimeException("Could not create directory " + path, e);
            }
        }

        if (!Files.isDirectory(path)) {
            throw new RuntimeException(path + " is not a directory!");
        }

        try {
            Files.walkFileTree(this.path, EnumSet.of(FileVisitOption.FOLLOW_LINKS), depth, new SimpleFileVisitor<
                Path>()
            {
                @Override
                public @NonNull FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) {
                    if (isValidFile(file) && (subpath == null || file.getParent().endsWith(subpath))) {
                        out.accept(file, requiresRemap, ModSource.UNKNOWN);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Exception while searching for mods in '" + path + "'!", e);
        }
    }
}
