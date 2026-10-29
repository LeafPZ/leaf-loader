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

import java.util.Collection;
import java.util.HashSet;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import dev.aoqia.leaf.api.Environment;
import dev.aoqia.leaf.api.EnvironmentInterface;
import dev.aoqia.leaf.api.EnvironmentInterfaces;

/**
 * Scans a class for Environment and EnvironmentInterface annotations to figure out what needs to be stripped.
 */
public class EnvironmentStrippingData extends ClassVisitor {
    private static final String ENVIRONMENT_DESCRIPTOR = Type.getDescriptor(Environment.class);
    private static final String ENVIRONMENT_INTERFACE_DESCRIPTOR = Type.getDescriptor(EnvironmentInterface.class);
    private static final String ENVIRONMENT_INTERFACES_DESCRIPTOR = Type.getDescriptor(EnvironmentInterfaces.class);

    private final String envType;

    private boolean stripEntireClass = false;
    private final Collection<String> stripInterfaces = new HashSet<>();
    private final Collection<String> stripFields = new HashSet<>();
    private final Collection<String> stripMethods = new HashSet<>();

    public EnvironmentStrippingData(int api, String envType) {
        super(api);
        this.envType = envType;
    }

    public Collection<String> getStripFields() {
        return stripFields;
    }

    public Collection<String> getStripInterfaces() {
        return stripInterfaces;
    }

    public Collection<String> getStripMethods() {
        return stripMethods;
    }

    public boolean isEmpty() {
        return stripInterfaces.isEmpty() && stripFields.isEmpty() && stripMethods.isEmpty();
    }

    public boolean stripEntireClass() {
        return stripEntireClass;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        if (ENVIRONMENT_DESCRIPTOR.equals(descriptor)) {
            return new EnvironmentAnnotationVisitor(api, () -> stripEntireClass = true);
        } else if (ENVIRONMENT_INTERFACE_DESCRIPTOR.equals(descriptor)) {
            return new EnvironmentInterfaceAnnotationVisitor(api);
        } else if (ENVIRONMENT_INTERFACES_DESCRIPTOR.equals(descriptor)) {
            return new AnnotationVisitor(api) {
                @Override
                public AnnotationVisitor visitArray(String name) {
                    if ("value".equals(name)) {
                        return new AnnotationVisitor(api) {
                            @Override
                            public AnnotationVisitor visitAnnotation(String name, String descriptor) {
                                return new EnvironmentInterfaceAnnotationVisitor(api);
                            }
                        };
                    }

                    return null;
                }
            };
        }

        return null;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return new FieldVisitor(api) {
            @Override
            public AnnotationVisitor visitAnnotation(String annotationDescriptor, boolean visible) {
                return visitMemberAnnotation(annotationDescriptor, visible, () -> stripFields.add(name + descriptor));
            }
        };
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
        String[] exceptions)
    {
        String methodId = name + descriptor;
        return new MethodVisitor(api) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                return visitMemberAnnotation(descriptor, visible, () -> stripMethods.add(methodId));
            }
        };
    }

    private AnnotationVisitor visitMemberAnnotation(String descriptor, boolean visible, Runnable onEnvMismatch) {
        if (ENVIRONMENT_DESCRIPTOR.equals(descriptor)) {
            return new EnvironmentAnnotationVisitor(api, onEnvMismatch);
        }

        return null;
    }

    private class EnvironmentAnnotationVisitor extends AnnotationVisitor {
        private final Runnable onEnvMismatch;

        private EnvironmentAnnotationVisitor(int api, Runnable onEnvMismatch) {
            super(api);
            this.onEnvMismatch = onEnvMismatch;
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            if ("value".equals(name) && !envType.equals(value)) {
                onEnvMismatch.run();
            }
        }
    }

    private class EnvironmentInterfaceAnnotationVisitor extends AnnotationVisitor {
        private boolean envMismatch;
        private Type itf;

        private EnvironmentInterfaceAnnotationVisitor(int api) {
            super(api);
        }

        @Override
        public void visit(String name, Object value) {
            if ("itf".equals(name)) {
                itf = (Type) value;
            }
        }

        @Override
        public void visitEnd() {
            if (envMismatch) {
                stripInterfaces.add(itf.getInternalName());
            }
        }

        @Override
        public void visitEnum(String name, String descriptor, String value) {
            if ("value".equals(name) && !envType.equals(value)) {
                envMismatch = true;
            }
        }
    }
}
