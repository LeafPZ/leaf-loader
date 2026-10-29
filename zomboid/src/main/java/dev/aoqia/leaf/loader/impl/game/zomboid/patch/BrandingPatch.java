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

package dev.aoqia.leaf.loader.impl.game.zomboid.patch;

import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import dev.aoqia.leaf.loader.impl.FormattedException;
import dev.aoqia.leaf.loader.impl.game.zomboid.Hooks;
import dev.aoqia.leaf.loader.impl.game.patch.GamePatch;
import dev.aoqia.leaf.loader.impl.launch.LeafLauncher;
import dev.aoqia.leaf.loader.impl.util.log.Log;
import dev.aoqia.leaf.loader.impl.util.log.LogCategory;

public final class BrandingPatch extends GamePatch {
    @Override
    public void process(LeafLauncher launcher, Function<String, ClassNode> classSource, Consumer<ClassNode> classEmitter) {
        ClassNode gameWindowCls = classSource.apply("zombie.GameWindow");
        if (gameWindowCls == null) {
            throw new FormattedException("Game Patch", "Failed to apply branding patch: GameWindow class wasn't found");
        }

        ClassNode coreCls = classSource.apply("zombie.core.Core");
        if (coreCls == null) {
            throw new FormattedException("Game Patch", "Failed to apply branding patch: Core class wasn't found");
        }

        if (patchWindowString(gameWindowCls)) {
            classEmitter.accept(gameWindowCls);
        }

        if (patchVersionString(coreCls)) {
            classEmitter.accept(coreCls);
        }
    }

    private boolean patchWindowString(ClassNode gameWindowCls) {
        MethodNode initDisplayMethod = findMethod(gameWindowCls, method -> {
            return method.name.equals("InitDisplay") && method.desc.equals("()V") && isPublicStatic(method.access);
        });

        if (initDisplayMethod == null) {
            throw new FormattedException("Game Patch",
                "Failed to apply branding patch because GameWindow#InitDisplay was not found");
        }

        Log.debug(LogCategory.GAME_PATCH, "Applying brand name hook to %s#%s", gameWindowCls.name,
            initDisplayMethod.name);

        ListIterator<AbstractInsnNode> it = initDisplayMethod.instructions.iterator();
        while (it.hasNext()) {
            if (it.next().getOpcode() == Opcodes.LDC) {
                it.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Hooks.INTERNAL_NAME, "appendBrandingString",
                    "(Ljava/lang/String;)Ljava/lang/String;", false));
                it.next();

                return true;
            }
        }

        return false;
    }

    private boolean patchVersionString(ClassNode coreCls) {
        MethodNode getVersionMethod = findMethod(coreCls, method -> {
            return method.name.equals("getVersion")
                && method.desc.equals("()Ljava/lang/String;")
                && isPublicInstance(method.access);
        });

        if (getVersionMethod == null) {
            throw new FormattedException("Game Patch",
                "Failed to apply branding patch because Core#getVersion was not found");
        }

        Log.debug(LogCategory.GAME_PATCH, "Applying brand name hook to %s#%s", coreCls.name, getVersionMethod.name);

        ListIterator<AbstractInsnNode> it = getVersionMethod.instructions.iterator();
        while (it.hasNext()) {
            if (it.next().getOpcode() == Opcodes.ARETURN) {
                it.previous();
                it.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Hooks.INTERNAL_NAME, "appendBrandingString",
                    "(Ljava/lang/String;)Ljava/lang/String;", false));
                it.next();

                return true;
            }
        }

        return false;
    }
}
