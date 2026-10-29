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

package dev.aoqia.leaf.loader.impl.entrypoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import dev.aoqia.leaf.loader.api.EntrypointException;
import dev.aoqia.leaf.loader.api.entrypoint.EntrypointContainer;
import dev.aoqia.leaf.loader.impl.ModContainerImpl;
import dev.aoqia.leaf.loader.impl.metadata.EntrypointMetadata;
import dev.aoqia.leaf.loader.impl.util.log.Log;
import dev.aoqia.leaf.loader.impl.util.log.LogCategory;

public final class EntrypointStorage {
    private final Map<String, List<Entry>> entryMap = new HashMap<>();

    @SuppressWarnings("unchecked") // return value allows "throw" declaration to end method
    static <E extends Throwable> RuntimeException sneakyThrows(Throwable ex) throws E {
        throw (E) ex;
    }

    public void add(ModContainerImpl modContainer, String key, EntrypointMetadata metadata, Map<String,
        dev.aoqia.leaf.loader.api.LanguageAdapter> adapterMap) throws Exception
    {
        if (!adapterMap.containsKey(metadata.getAdapter())) {
            throw new Exception("Could not find adapter '" + metadata.getAdapter() + "' (mod " + modContainer
                .getMetadata()
                .getId() + "!)");
        }

        Log.debug(LogCategory.ENTRYPOINT, "Registering new-style initializer %s for mod %s (key %s)", metadata
            .getValue(), modContainer.getMetadata().getId(), key);
        getOrCreateEntries(key).add(new Entry(
            modContainer, adapterMap.get(metadata.getAdapter()), metadata.getValue()));
    }

    @SuppressWarnings("deprecation")
    public <T> List<EntrypointContainer<T>> getEntrypointContainers(String key, Class<T> type) {
        List<Entry> entries = entryMap.get(key);
        if (entries == null)
            return Collections.emptyList();

        List<EntrypointContainer<T>> results = new ArrayList<>(entries.size());
        EntrypointException exc = null;

        for (Entry entry : entries) {
            EntrypointContainerImpl<T> container;

            if (entry.isOptional()) {
                try {
                    T instance = entry.getOrCreate(type);
                    if (instance == null)
                        continue;

                    container = new EntrypointContainerImpl<>(entry, instance);
                } catch (Throwable t) {
                    if (exc == null) {
                        exc = new EntrypointException(key, entry.getModContainer().getMetadata().getId(), t);
                    } else {
                        exc.addSuppressed(t);
                    }

                    continue;
                }
            } else {
                container = new EntrypointContainerImpl<>(key, type, entry);
            }

            results.add(container);
        }

        if (exc != null)
            throw exc;

        return results;
    }

    @SuppressWarnings("deprecation")
    public <T> List<T> getEntrypoints(String key, Class<T> type) {
        List<Entry> entries = entryMap.get(key);
        if (entries == null)
            return Collections.emptyList();

        EntrypointException exception = null;
        List<T> results = new ArrayList<>(entries.size());

        for (Entry entry : entries) {
            try {
                T result = entry.getOrCreate(type);

                if (result != null) {
                    results.add(result);
                }
            } catch (Throwable t) {
                if (exception == null) {
                    exception = new EntrypointException(key, entry.getModContainer().getMetadata().getId(), t);
                } else {
                    exception.addSuppressed(t);
                }
            }
        }

        if (exception != null) {
            throw exception;
        }

        return results;
    }

    public boolean hasEntrypoints(String key) {
        return entryMap.containsKey(key);
    }

    private List<Entry> getOrCreateEntries(String key) {
        return entryMap.computeIfAbsent(key, (z) -> new ArrayList<>());
    }

    static final class Entry {
        private final ModContainerImpl mod;
        private final dev.aoqia.leaf.loader.api.LanguageAdapter adapter;
        private final String value;
        private final Map<Class<?>, Object> instanceMap;

        Entry(ModContainerImpl mod, dev.aoqia.leaf.loader.api.LanguageAdapter adapter, String value) {
            this.mod = mod;
            this.adapter = adapter;
            this.value = value;
            this.instanceMap = new IdentityHashMap<>(1);
        }

        public String getDefinition() {
            return value;
        }

        public ModContainerImpl getModContainer() {
            return mod;
        }

        @SuppressWarnings("unchecked")
        public synchronized <T> T getOrCreate(Class<T> type) throws Exception {
            // this impl allows reentrancy (unlike computeIfAbsent)
            T ret = (T) instanceMap.get(type);

            if (ret == null) {
                ret = adapter.create(mod, value, type);
                assert ret != null;
                T prev = (T) instanceMap.putIfAbsent(type, ret);
                if (prev != null)
                    ret = prev;
            }

            return ret;
        }

        public boolean isOptional() {
            return false;
        }

        @Override
        public String toString() {
            return mod.getMetadata().getId() + "->(0.3.x)" + value;
        }
    }
}
