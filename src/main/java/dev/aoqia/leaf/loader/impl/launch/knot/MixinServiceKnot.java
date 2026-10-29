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

package dev.aoqia.leaf.loader.impl.launch.knot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.IAdviceProvider;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IFeatureValidator;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.IMixinInternal;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.ITransformer;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.util.ReEntranceLock;

import dev.aoqia.leaf.loader.impl.launch.LeafLauncherBase;
import dev.aoqia.leaf.loader.impl.launch.LeafMixinVersions;
import dev.aoqia.leaf.loader.impl.util.UrlUtil;

public class MixinServiceKnot implements IMixinService, IClassProvider, IClassBytecodeProvider, ITransformerProvider,
    IClassTracker
{
    static IMixinTransformer transformer;

    private final ReEntranceLock lock;

    public MixinServiceKnot() {
        lock = new ReEntranceLock(1);
    }

    static IMixinTransformer getTransformer() {
        return transformer;
    }

    @Override
    public void addTransformerExclusion(String name) {
    }

    @Override
    public void beginPhase() {
    }

    @Override
    public void checkEnv(Object bootSource) {
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Knot.class.getClassLoader());
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return LeafLauncherBase.getLauncher().getTargetClassLoader().loadClass(name);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, LeafLauncherBase.getLauncher().getTargetClassLoader());
    }

    @Override
    public IAdviceProvider getAdviceProvider() {
        return new IAdviceProvider() {
            @Override
            public String higherCompatibilityNeeded(int requiredCompatibility, String requiredCompatibilityString) {
                return "Increase your Leaf Loader dependency to at least " + LeafMixinVersions.getMinLoaderVersion(
                    requiredCompatibility);
            }
        };
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return this;
    }

    public byte[] getClassBytes(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        byte[] classBytes = LeafLauncherBase.getLauncher().getClassByteArray(name, runTransformers);

        if (classBytes != null) {
            return classBytes;
        } else {
            throw new ClassNotFoundException(name);
        }
    }

    public byte[] getClassBytes(String name, String transformedName) throws IOException {
        return LeafLauncherBase.getLauncher().getClassByteArray(name, true);
    }

    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return getClassNode(name, true);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return getClassNode(name, runTransformers, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws ClassNotFoundException,
        IOException
    {
        ClassReader reader = new ClassReader(getClassBytes(name, runTransformers));
        ClassNode node = new ClassNode();
        reader.accept(node, readerFlags);
        return node;
    }

    @Override
    public URL[] getClassPath() {
        // Mixin 0.7.x only uses getClassPath() to find itself; we implement CodeSource correctly,
        // so this is unnecessary.
        return new URL[0];
    }

    @Override
    public IClassProvider getClassProvider() {
        return this;
    }

    @Override
    public String getClassRestrictions(String className) {
        return "";
    }

    @Override
    public IClassTracker getClassTracker() {
        return this;
    }

    @Override
    public Collection<ITransformer> getDelegatedTransformers() {
        return Collections.emptyList();
    }

    @Override
    public IFeatureValidator getFeatureValidator() {
        return IFeatureValidator.ALLOW_ALL;
    }

    @Override
    public MixinEnvironment.Phase getInitialPhase() {
        return MixinEnvironment.Phase.PREINIT;
    }

    @Override
    public ILogger getLogger(String name) {
        return MixinLogger.get(name);
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_25;
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_8;
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return LeafLauncherBase.getLauncher() instanceof Knot ? "Knot/Leaf" : "Launchwrapper/Leaf";
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return Collections.singletonList("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        return new ContainerHandleURI(UrlUtil.LOADER_CODE_SOURCE.toUri());
    }

    @Override
    public ReEntranceLock getReEntranceLock() {
        return lock;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return LeafLauncherBase.getLauncher().getResourceAsStream(name);
    }

    @Override
    public String getSideName() {
        return LeafLauncherBase.getLauncher().getEnvironmentType().name();
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return this;
    }

    @Override
    public Collection<ITransformer> getTransformers() {
        return Collections.emptyList();
    }

    @Override
    public void init() {
    }

    @Override
    public boolean isClassLoaded(String className) {
        return LeafLauncherBase.getLauncher().isClassLoaded(className);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory) {
            transformer = ((IMixinTransformerFactory) internal).createTransformer();
        }
    }

    @Override
    public void prepare() {
    }

    @Override
    public void registerInvalidClass(String className) {
    }
}
