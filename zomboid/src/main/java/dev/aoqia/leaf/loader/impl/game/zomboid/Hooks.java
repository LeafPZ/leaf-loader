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

import java.io.File;

import dev.aoqia.leaf.api.ClientModInitializer;
import dev.aoqia.leaf.api.ServerModInitializer;
import dev.aoqia.leaf.api.ModInitializer;
import dev.aoqia.leaf.loader.impl.LeafLoaderImpl;
import dev.aoqia.leaf.loader.impl.launch.LeafLauncher;
import dev.aoqia.leaf.loader.impl.launch.LeafLauncherBase;
import dev.aoqia.leaf.loader.impl.util.log.Log;
import dev.aoqia.leaf.loader.impl.util.log.LogCategory;

public final class Hooks {
	public static final String INTERNAL_NAME = Hooks.class.getName().replace('.', '/');
    public static final String LEAF = "leaf";

    public static String appendBrandingString(final String s) {
        if (s == null || s.isEmpty()) {
            Log.warn(LogCategory.GAME_PROVIDER, "Null or empty input string found!", new IllegalStateException());
            return LEAF;
		}

        return s + " (" + LEAF + " " + LeafLoaderImpl.VERSION + ")";
	}

	public static void startClient(File runDir, Class<?> gameInstance) {
		if (runDir == null) {
			runDir = new File(".");
		}

		LeafLoaderImpl loader = LeafLoaderImpl.INSTANCE;
		loader.prepareModInit(runDir.toPath(), gameInstance);
		loader.invokeEntrypoints("main", ModInitializer.class, ModInitializer::onInitialize);
		loader.invokeEntrypoints("client", ClientModInitializer.class, ClientModInitializer::onInitializeClient);
	}

	public static void startServer(File runDir, Class<?> gameInstance) {
		if (runDir == null) {
			runDir = new File(".");
		}

		LeafLoaderImpl loader = LeafLoaderImpl.INSTANCE;
		loader.prepareModInit(runDir.toPath(), gameInstance);
		loader.invokeEntrypoints("main", ModInitializer.class, ModInitializer::onInitialize);
		loader.invokeEntrypoints("server", ServerModInitializer.class, ServerModInitializer::onInitializeServer);
	}

	public static void setGameInstance(Object gameInstance) {
		LeafLoaderImpl.INSTANCE.setGameInstance(gameInstance);
	}

    /**
     * @see ZomboidGameProvider#setupLogHandler(LeafLauncher, boolean)
     */
    public static void setupLogHandler() {
		((ZomboidGameProvider) LeafLoaderImpl.INSTANCE.getGameProvider())
            .setupLogHandler(LeafLauncherBase.getLauncher(), true);
    }
}
