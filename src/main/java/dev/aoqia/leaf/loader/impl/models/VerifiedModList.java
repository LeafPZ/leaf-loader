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
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import dev.aoqia.leaf.loader.impl.FormattedException;
import dev.aoqia.leaf.loader.impl.discovery.ModCandidateImpl;

public class VerifiedModList {
    public static final int VERSION = 1;

    private final Path path;
    private final List<Mod> internal;

    public VerifiedModList(Path path) {
        this.path = path;
        this.internal = new ArrayList<>();
    }

    public void add(ModCandidateImpl mod) {
        this.internal.add(new Mod(mod));
    }

    public boolean contains(ModCandidateImpl mod) {
        return this.internal.stream()
            .anyMatch(m -> {
                String workshopId = m.getWorkshopId();
                return workshopId != null
                    && workshopId.equals(mod.getWorkshopId())
                    && m.getGameId().equals(mod.getGameId())
                    && m.getId().equals(mod.getId())
                    && m.getJarHash().equals(mod.getJarHash());
            });
    }

    public boolean isVerified(ModCandidateImpl mod) {
        return contains(mod);
    }

    public void readOrCreate() throws IOException {
        // If the mod list data file doesn't exist, initialise it and return empty.
        if (!Files.exists(this.path)) {
            if (!Files.exists(this.path.getParent())) {
                Files.createDirectory(this.path.getParent());
            }

            Files.write(this.path, (VERSION + "\n").getBytes());
            return;
        }

        try (BufferedReader br = Files.newBufferedReader(this.path)) {
            int version = Character.getNumericValue(br.readLine().charAt(0));
            if (version != VERSION) {
                br.close();
                Files.write(this.path, (VERSION + "\n").getBytes());
                return;
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] components = line.split("\t");
                if (components.length != 4) {
                    throw new IllegalStateException("Parsed bad verified mod list file");
                }

                String workshopId = components[0];
                String gameModId = components[1];
                String leafModId = components[2];
                String jarHash = components[3];

                this.internal.add(new Mod(workshopId, leafModId, gameModId, jarHash));
            }
        }
    }

    public void updateMod(ModCandidateImpl mod) {
        Optional<Mod> stored = this.internal.stream()
            .filter(m -> {
                String workshopId = m.getWorkshopId();
                return workshopId != null && workshopId.equals(mod.getWorkshopId());
            })
            .findFirst();

        if (!stored.isPresent()) {
            throw new FormattedException("Failed to update mod %s (%s/%s) after verifying", mod.getId(),
                mod.getWorkshopId(), mod.getGameId());
        }

        stored.get().setWorkshopId(mod.getWorkshopId());
        stored.get().setId(mod.getId());
        stored.get().setGameId(mod.getGameId());
        stored.get().setJarHash(mod.getJarHash());
    }

    public boolean wasVerified(ModCandidateImpl mod) {
        return containsWithBadHash(mod);
    }

    public void write() throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(this.path, StandardOpenOption.CREATE)) {
            bw.write(VERSION + "\n");
            for (Mod mod : this.internal) {
                bw.write(mod.getWorkshopId()
                    + "\t" + mod.getGameId()
                    + "\t" + mod.getId()
                    + "\t" + mod.getJarHash()
                    + "\n");
            }
        }
    }

    private boolean containsIgnoreHash(ModCandidateImpl mod) {
        return this.internal.stream()
            .anyMatch(m -> {
                String workshopId = m.getWorkshopId();
                return workshopId != null
                    && workshopId.equals(mod.getWorkshopId())
                    && m.getGameId().equals(mod.getGameId())
                    && m.getId().equals(mod.getId());
            });
    }

    private boolean containsWithBadHash(ModCandidateImpl mod) {
        return this.internal.stream()
            .anyMatch(m -> {
                String workshopId = m.getWorkshopId();
                return workshopId != null
                    && workshopId.equals(mod.getWorkshopId())
                    && m.getGameId().equals(mod.getGameId())
                    && m.getId().equals(mod.getId())
                    && !m.getJarHash().equals(mod.getJarHash());
            });
    }

    public static class Mod {
        private @Nullable String workshopId;
        private String id;
        private String gameId;
        private String jarHash;

        public Mod(ModCandidateImpl candidate) {
            this.workshopId = candidate.getWorkshopId();
            this.id = candidate.getId();
            this.gameId = candidate.getGameId();
            this.jarHash = candidate.getJarHash();
        }

        public Mod(@Nullable String workshopId, String id, String gameId, String jarHash) {
            this.workshopId = workshopId;
            this.id = id;
            this.gameId = gameId;
            this.jarHash = jarHash;
        }

        public String getGameId() {
            return gameId;
        }

        public String getId() {
            return id;
        }

        public String getJarHash() {
            return jarHash;
        }

        public @Nullable String getWorkshopId() {
            return workshopId;
        }

        public void setGameId(String gameId) {
            this.gameId = gameId;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setJarHash(String jarHash) {
            this.jarHash = jarHash;
        }

        public void setWorkshopId(@Nullable String workshopId) {
            this.workshopId = workshopId;
        }
    }
}
