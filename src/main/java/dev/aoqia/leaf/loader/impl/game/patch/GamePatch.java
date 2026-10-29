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

package dev.aoqia.leaf.loader.impl.game.patch;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import dev.aoqia.leaf.loader.impl.launch.LeafLauncher;

public abstract class GamePatch {
    public abstract void process(LeafLauncher launcher, Function<String, ClassNode> classSource, Consumer<
        ClassNode> classEmitter);

    protected FieldNode findField(ClassNode node, Predicate<FieldNode> predicate) {
        return node.fields.stream().filter(predicate).findAny().orElse(null);
    }

    protected List<FieldNode> findFields(ClassNode node, Predicate<FieldNode> predicate) {
        return node.fields.stream().filter(predicate).collect(Collectors.toList());
    }

    protected AbstractInsnNode findInsn(MethodNode node, Predicate<AbstractInsnNode> predicate, boolean last) {
        if (last) {
            for (int i = node.instructions.size() - 1; i >= 0; i--) {
                AbstractInsnNode insn = node.instructions.get(i);

                if (predicate.test(insn)) {
                    return insn;
                }
            }
        } else {
            for (int i = 0; i < node.instructions.size(); i++) {
                AbstractInsnNode insn = node.instructions.get(i);

                if (predicate.test(insn)) {
                    return insn;
                }
            }
        }

        return null;
    }

    protected MethodNode findMethod(ClassNode node, Predicate<MethodNode> predicate) {
        return node.methods.stream().filter(predicate).findAny().orElse(null);
    }

    protected boolean isPublicInstance(int access) {
        // noinspection PointlessBitwiseExpression
        return ((access & 0x0F) == (Opcodes.ACC_PUBLIC | 0 /* non-static */));
    }

    protected boolean isPublicStatic(int access) {
        return ((access & 0x0F) == (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC));
    }

    protected boolean isStatic(int access) {
        return ((access & Opcodes.ACC_STATIC) != 0);
    }

    protected void moveAfter(ListIterator<AbstractInsnNode> it, AbstractInsnNode targetNode) {
        while (it.hasNext()) {
            AbstractInsnNode node = it.next();

            if (node == targetNode) {
                break;
            }
        }
    }

    protected void moveAfter(ListIterator<AbstractInsnNode> it, int opcode) {
        while (it.hasNext()) {
            AbstractInsnNode node = it.next();

            if (node.getOpcode() == opcode) {
                break;
            }
        }
    }

    protected void moveBefore(ListIterator<AbstractInsnNode> it, AbstractInsnNode targetNode) {
        moveAfter(it, targetNode);
        it.previous();
    }

    protected void moveBefore(ListIterator<AbstractInsnNode> it, int opcode) {
        moveAfter(it, opcode);
        it.previous();
    }

    protected void moveBeforeType(ListIterator<AbstractInsnNode> it, int nodeType) {
        while (it.hasPrevious()) {
            AbstractInsnNode node = it.previous();

            if (node.getType() == nodeType) {
                break;
            }
        }
    }
}
