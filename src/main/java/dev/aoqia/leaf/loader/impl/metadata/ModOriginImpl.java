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

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import dev.aoqia.leaf.loader.api.metadata.ModOrigin;

public final class ModOriginImpl implements ModOrigin {
    private final Kind kind;
    private List<Path> paths;
    private String parentModId;
    private String parentSubLocation;

    public ModOriginImpl() {
        this.kind = Kind.UNKNOWN;
    }

    public ModOriginImpl(List<Path> paths) {
        this.kind = Kind.PATH;
        this.paths = paths;
    }

    public ModOriginImpl(String parentModId, String parentSubLocation) {
        this.kind = Kind.NESTED;
        this.parentModId = parentModId;
        this.parentSubLocation = parentSubLocation;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getParentModId() {
        if (kind != Kind.NESTED) {
            throw new UnsupportedOperationException("kind " + kind.name() + " doesn't have a parent mod");
        }

        return parentModId;
    }

    @Override
    public String getParentSubLocation() {
        if (kind != Kind.NESTED) {
            throw new UnsupportedOperationException("kind " + kind.name() + " doesn't have a parent sub-location");
        }

        return parentSubLocation;
    }

    @Override
    public List<Path> getPaths() {
        if (kind != Kind.PATH) {
            throw new UnsupportedOperationException("kind " + kind.name() + " doesn't have paths");
        }

        return paths;
    }

    @Override
    public String toString() {
        switch (getKind()) {
            case PATH:
                return paths.stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator));
            case NESTED:
                return String.format("%s:%s", parentModId, parentSubLocation);
            default:
                return "unknown";
        }
    }
}
