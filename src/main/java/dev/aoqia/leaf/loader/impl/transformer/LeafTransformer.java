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

package dev.aoqia.leaf.loader.impl.transformer;

import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import dev.aoqia.leaf.api.EnvType;
import dev.aoqia.leaf.loader.impl.LeafLoaderImpl;
import dev.aoqia.leaf.loader.impl.game.GameProvider.BuiltinTransform;
import dev.aoqia.leaf.loader.impl.launch.LeafLauncherBase;

public final class LeafTransformer {
    public static byte[] transform(boolean isDevelopment, EnvType envType, String name, byte[] bytes) {
        Set<BuiltinTransform> transforms = LeafLoaderImpl.INSTANCE.getGameProvider().getBuiltinTransforms(name);
        boolean transformAccess = transforms.contains(BuiltinTransform.WIDEN_ALL_PACKAGE_ACCESS) && LeafLauncherBase
            .getLauncher()
            .getMappingConfiguration()
            .requiresPackageAccessHack();
        boolean environmentStrip = transforms.contains(BuiltinTransform.STRIP_ENVIRONMENT);
        boolean applyClassTweaker = transforms.contains(BuiltinTransform.CLASS_TWEAKS) && LeafLoaderImpl.INSTANCE
            .getClassTweaker()
            .getTargets()
            .contains(name.replace('.', '/'));

        if (!transformAccess && !environmentStrip && !applyClassTweaker) {
            return bytes;
        }

        ClassReader classReader = new ClassReader(bytes);
        ClassWriter classWriter = new ClassWriter(classReader, 0);
        ClassVisitor visitor = classWriter;
        int visitorCount = 0;

        if (applyClassTweaker) {
            // TODO: generated classes?
            visitor =
                LeafLoaderImpl.INSTANCE.getClassTweaker().createClassVisitor(LeafLoaderImpl.ASM_VERSION, visitor, null);
            visitorCount++;
        }

        if (transformAccess) {
            visitor = new PackageAccessFixer(LeafLoaderImpl.ASM_VERSION, visitor);
            visitorCount++;
        }

        if (environmentStrip) {
            EnvironmentStrippingData stripData =
                new EnvironmentStrippingData(LeafLoaderImpl.ASM_VERSION, envType.toString());
            classReader.accept(stripData, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);

            if (stripData.stripEntireClass()) {
                throw new RuntimeException("Cannot load class " + name + " in environment type " + envType);
            }

            if (!stripData.isEmpty()) {
                visitor = new ClassStripper(LeafLoaderImpl.ASM_VERSION, visitor, stripData.getStripInterfaces(),
                    stripData.getStripFields(), stripData.getStripMethods());
                visitorCount++;
            }
        }

        if (visitorCount <= 0) {
            return bytes;
        }

        classReader.accept(visitor, 0);
        return classWriter.toByteArray();
    }
}
