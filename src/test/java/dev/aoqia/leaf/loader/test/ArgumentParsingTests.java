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

package dev.aoqia.leaf.loader.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.aoqia.leaf.loader.impl.util.Arguments;

public class ArgumentParsingTests {
    @Test
    public void parseJvm() {
        Arguments arguments = new Arguments();
        arguments.parse(new String[] { "-Xmx8192m", "--" });
        arguments.put("versionType", "Leaf");

        Assertions.assertEquals(1, arguments.getValueArgs().size());
        Assertions.assertNull(arguments.get("xuid"));
        Assertions.assertEquals("Leaf", arguments.get("versionType"));
    }

    @Test
    public void parseMissing() {
        Arguments arguments = new Arguments();
        arguments.parse(new String[] { "-debug", "-pzexeconfig", "-novoip" });
        arguments.put("versionType", "Leaf");

        Assertions.assertEquals(1, arguments.getValueArgs().size());
        Assertions.assertNull(arguments.get("xuid"));
        Assertions.assertNull(arguments.get("pzexeconfig"));
        Assertions.assertEquals("Leaf", arguments.get("versionType"));
    }

    @Test
    public void parseNormal() {
        Arguments arguments = new Arguments();
        arguments.parse(new String[] { "-debug", "-pzexeconfig", "SomeConfig.json", "-novoip" });
        arguments.put("versionType", "Leaf");

        Assertions.assertEquals(2, arguments.getValueArgs().size());
        Assertions.assertEquals("SomeConfig.json", arguments.get("pzexeconfig"));
        Assertions.assertEquals("Leaf", arguments.get("versionType"));
    }
}
