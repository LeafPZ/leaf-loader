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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import dev.aoqia.leaf.api.EnvType;
import dev.aoqia.leaf.loader.impl.game.patch.GamePatch;
import dev.aoqia.leaf.loader.impl.game.zomboid.Hooks;
import dev.aoqia.leaf.loader.impl.launch.LeafLauncher;
import dev.aoqia.leaf.loader.impl.util.log.Log;
import dev.aoqia.leaf.loader.impl.util.log.LogCategory;

public class EntrypointPatch extends GamePatch {
    @Override
    public void process(LeafLauncher launcher, Function<String, ClassNode> classSource,
        Consumer<ClassNode> classEmitter)
    {
        EnvType type = launcher.getEnvironmentType();
        String entrypoint = launcher.getEntrypoint();
        if (!entrypoint.startsWith("zombie.")) {
            return;
        }

        ClassNode mainClass = classSource.apply(entrypoint);

        if (mainClass == null) {
            throw new RuntimeException("Could not load main class " + entrypoint + "!");
        }

        // Main game entrypoint search. Hook just under DebugLog.init() call in both enviros.

        // Find the main method in MainScreenState.
        MethodNode mainMethod = findMethod(mainClass, (method) -> {
            return method.name.equals("main")
                && method.desc.equals("([Ljava/lang/String;)V")
                && isPublicStatic(method.access);
        });

        if (mainMethod == null) {
            throw new RuntimeException("Could not find main method in " + entrypoint + "!");
        }

        // Attempt to find the DebugLog.init() function call and store the instruction.
        MethodInsnNode debugLogInitInsn = (MethodInsnNode) findInsn(mainMethod, (insn) -> {
            return insn instanceof MethodInsnNode
                && insn.getOpcode() == Opcodes.INVOKEVIRTUAL
                && ((MethodInsnNode) insn).name.equals("init")
                && ((MethodInsnNode) insn).owner.equals("zombie/debug/DebugLog");
        }, false);
        if (debugLogInitInsn == null) {
            throw new RuntimeException("Could not find DebugLog init method in " + entrypoint + "!");
        }

        Log.debug(LogCategory.GAME_PATCH, "Patching main function %s%s", mainMethod.name, mainMethod.desc);

        ListIterator<AbstractInsnNode> iter = mainMethod.instructions.iterator();
        moveAfter(iter, debugLogInitInsn);

        iter.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Hooks.INTERNAL_NAME, "setupLogHandler", "()V"));

        // Set up ZomboidFileSystem.instance.getCacheDir() and construct a File object to pass as first arg
        iter.add(new TypeInsnNode(Opcodes.NEW, "java/io/File"));
        iter.add(new InsnNode(Opcodes.DUP));
        iter.add(
            new FieldInsnNode(Opcodes.GETSTATIC, "zombie/ZomboidFileSystem", "instance", "Lzombie/ZomboidFileSystem;"));
        iter.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "zombie/ZomboidFileSystem", "getCacheDir",
            "()Ljava/lang/String;"));
        iter.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false));
        // Set up the class itself as the second arg.
        iter.add(new LdcInsnNode(Type.getObjectType(entrypoint.replace(".", "/"))));
        // Add the final startClient/startServer method call.
        iter.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            Hooks.INTERNAL_NAME,
            type == EnvType.CLIENT ? "startClient" : "startServer",
            "(Ljava/io/File;Ljava/lang/Class;)V",
            false));

        classEmitter.accept(mainClass);
    }
}
