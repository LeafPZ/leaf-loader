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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dev.aoqia.leaf.loader.api.Version;
import dev.aoqia.leaf.loader.api.VersionParsingException;
import dev.aoqia.leaf.loader.api.metadata.ModDependency;
import dev.aoqia.leaf.loader.api.metadata.version.VersionInterval;
import dev.aoqia.leaf.loader.api.metadata.version.VersionPredicate;

public final class ModDependencyImpl implements ModDependency {
    private Kind kind;
    private final String modId;
    private final List<String> matcherStringList;
    private final Collection<VersionPredicate> ranges;

    public ModDependencyImpl(Kind kind, String modId, List<String> matcherStringList) throws VersionParsingException {
        this.kind = kind;
        this.modId = modId;
        this.matcherStringList = matcherStringList;
        this.ranges = VersionPredicate.parse(this.matcherStringList);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ModDependency))
            return false;

        ModDependency o = (ModDependency) obj;

        return kind == o.getKind()
            && modId.equals(o.getModId())
            && ranges.equals(o.getVersionRequirements());
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public String getModId() {
        return this.modId;
    }

    @Override
    public List<VersionInterval> getVersionIntervals() {
        List<VersionInterval> ret = Collections.emptyList();

        for (VersionPredicate predicate : ranges) {
            ret = VersionInterval.or(ret, predicate.getInterval());
        }

        return ret;
    }

    @Override
    public Collection<VersionPredicate> getVersionRequirements() {
        return ranges;
    }

    @Override
    public int hashCode() {
        return (kind.ordinal() * 31 + modId.hashCode()) * 257 + ranges.hashCode();
    }

    @Override
    public boolean matches(Version version) {
        for (VersionPredicate predicate : ranges) {
            if (predicate.test(version))
                return true;
        }

        return false;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("{");
        builder.append(kind.getKey());
        builder.append(' ');
        builder.append(this.modId);
        builder.append(" @ [");

        for (int i = 0; i < matcherStringList.size(); i++) {
            if (i > 0) {
                builder.append(" || ");
            }

            builder.append(matcherStringList.get(i));
        }

        builder.append("]}");
        return builder.toString();
    }
}
