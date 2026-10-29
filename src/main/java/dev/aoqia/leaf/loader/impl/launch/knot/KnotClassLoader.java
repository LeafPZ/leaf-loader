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
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Objects;

import dev.aoqia.leaf.api.EnvType;
import dev.aoqia.leaf.loader.impl.game.GameProvider;
import dev.aoqia.leaf.loader.impl.launch.knot.KnotClassDelegate.ClassLoaderAccess;
import dev.aoqia.leaf.loader.impl.mrj.AbstractSecureClassLoader;

// class name referenced by string constant in dev.aoqia.leaf.loader.impl.util.LoaderUtil.verifyNotInTargetCl
final class KnotClassLoader extends AbstractSecureClassLoader implements ClassLoaderAccess {
    static {
        registerAsParallelCapable();
    }

    private final DynamicURLClassLoader urlLoader;
    private final ClassLoader originalLoader;
    private final KnotClassDelegate<KnotClassLoader> delegate;

    KnotClassLoader(boolean isDevelopment, EnvType envType, GameProvider provider) {
        super("knot", new DynamicURLClassLoader(new URL[0]));
        this.originalLoader = getClass().getClassLoader();
        this.urlLoader = (DynamicURLClassLoader) getParent();
        this.delegate = new KnotClassDelegate<>(isDevelopment, envType, this, originalLoader, provider);
    }

    @Override
    public void addUrlFwd(URL url) {
        urlLoader.addURL(url);
    }

    @Override
    public Class<?> defineClassFwd(String name, byte[] b, int off, int len, CodeSource cs) {
        return super.defineClass(name, b, off, len, cs);
    }

    @Override
    public Package definePackageFwd(String name, String specTitle, String specVersion, String specVendor,
        String implTitle, String implVersion, String implVendor, URL sealBase) throws IllegalArgumentException
    {
        return super.definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor,
            sealBase);
    }

    @Override
    public Class<?> findLoadedClassFwd(String name) {
        return super.findLoadedClass(name);
    }

    @Override
    public URL findResource(String name) {
        Objects.requireNonNull(name);

        return urlLoader.findResource(name);
    }

    @Override
    public URL findResourceFwd(String name) {
        return urlLoader.findResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Objects.requireNonNull(name);

        return urlLoader.findResources(name);
    }

    @Override
    public Object getClassLoadingLockFwd(String name) {
        return super.getClassLoadingLock(name);
    }

    @Override
    public Package getPackageFwd(String name) {
        return super.getPackage(name);
    }

    @Override
    public URL getResource(String name) {
        Objects.requireNonNull(name);

        URL url = urlLoader.getResource(name);

        if (url == null) {
            url = originalLoader.getResource(name);
        }

        return url;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        Objects.requireNonNull(name);

        InputStream inputStream = urlLoader.getResourceAsStream(name);

        if (inputStream == null) {
            inputStream = originalLoader.getResourceAsStream(name);
        }

        return inputStream;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Objects.requireNonNull(name);

        final Enumeration<URL> resources = urlLoader.getResources(name);

        if (!resources.hasMoreElements()) {
            return originalLoader.getResources(name);
        }

        return resources;
    }

    @Override
    public void resolveClassFwd(Class<?> cls) {
        super.resolveClass(cls);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return delegate.tryLoadClass(name, false);
    }

    @Override
    protected String findLibrary(String libname) {
        return delegate.findLibrary(libname);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return delegate.loadClass(name, resolve);
    }

    KnotClassDelegate<?> getDelegate() {
        return delegate;
    }

    private static final class DynamicURLClassLoader extends URLClassLoader {
        static {
            registerAsParallelCapable();
        }

        private DynamicURLClassLoader(URL[] urls) {
            super(urls, new DummyClassLoader());
        }

        @Override
        public void addURL(URL url) {
            super.addURL(url);
        }
    }
}
