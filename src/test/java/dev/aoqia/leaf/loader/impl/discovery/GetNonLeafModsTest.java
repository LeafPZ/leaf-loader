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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import dev.aoqia.leaf.api.EnvType;
import dev.aoqia.leaf.loader.impl.LeafLoaderImpl;
import dev.aoqia.leaf.loader.impl.game.GameProvider;
import dev.aoqia.leaf.loader.impl.launch.LeafLauncher;
import dev.aoqia.leaf.loader.impl.launch.LeafLauncherBase;
import dev.aoqia.leaf.loader.impl.util.LoaderUtil;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetNonLeafModsTest {
    private LeafLoaderImpl loader;
    private ModDiscoverer discoverer;
    private MockedConstruction<LeafLoaderImpl> loaderConstruction;

    /*
     * Set up the mock loader and discoverer
     */
    @BeforeEach
    public void setUp() {
        GameProvider provider = mock();
        when(provider.getBuiltinMods()).thenReturn(Collections.emptyList());

        LeafLauncher launcher = mock();
        when(launcher.getEnvironmentType()).thenReturn(EnvType.CLIENT);
        when(launcher.isDevelopment()).thenReturn(false);
        LeafLauncherBase.setLauncher(launcher);
        loader = mock();
        when(loader.getGameProvider()).thenReturn(provider);
        when(loader.isDevelopmentEnvironment()).thenReturn(false);

        loaderConstruction = Mockito.mockConstructionWithAnswer(LeafLoaderImpl.class, invocation -> loader);

        discoverer = new ModDiscoverer(mock(), mock());
        discoverer.addCandidateFinder(new MockCandidateFinder());
    }

    @AfterEach
    public void tearDown() {
        loaderConstruction.close();
    }

    /*
     * Test that the discoverer can find non-fabric mods
     */
    @Test
    public void testGetNonLeafMods() throws ModResolutionException {
        List<ModCandidateImpl> acceptedMods = discoverer.discoverMods(loader, new HashMap<>());

        boolean foundDummyMod = false;
        for (ModCandidateImpl acceptedMod : acceptedMods) {
            if (acceptedMod.getId().equals("dummy")) {
                foundDummyMod = true;
                break;
            }
        }

        List<Path> nonLeafMods = discoverer.getNonLeafMods();
        Assertions.assertEquals(1, nonLeafMods.size());
        Assertions.assertEquals(
            Paths.get("src/test/resources/testing/discovery/dummyNonLeafMod.jar").toAbsolutePath(),
            nonLeafMods.get(0));
    }

    /*
     * Mock candidate finder that returns two dummy mods (one fabric and one
     * non-fabric)
     */
    public static class MockCandidateFinder implements ModCandidateFinder {
        @Override
        public void findCandidates(ModCandidateConsumer out) {
            out.accept(LoaderUtil.normalizePath(
                Paths.get("src/test/resources/testing/discovery/dummyLeafMod.jar")), false, ModSource.INTERNAL);
            out.accept(LoaderUtil.normalizePath(
                Paths.get("src/test/resources/testing/discovery/dummyNonLeafMod.jar")), false, ModSource.INTERNAL);
        }
    }
}
