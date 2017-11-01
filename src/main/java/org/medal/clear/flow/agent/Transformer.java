/*
 * Copyright 2017 skrymets.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.medal.clear.flow.agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import org.medal.clear.flow.agent.transformers.VisitClassTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Objects.requireNonNull;

/**
 *
 * @author skrymets
 */
class Transformer implements ClassFileTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(Transformer.class);

    private final Set<String> packagesToInstrument;

    private final Set<ClassTransformer> transformers = new HashSet<>(
            Arrays.asList(
                    // new DefaultClassTransformer(),
                    new VisitClassTransformer()
            )
    );

    Transformer(Set<String> packagesToInstrument) {
        this.packagesToInstrument = firstNonNull(
                packagesToInstrument,
                Collections.<String>emptySet()
        );
    }

    @Override
    public byte[] transform(
            final ClassLoader loader,
            final String className,
            final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain,
            final byte[] classfileBuffer) throws IllegalClassFormatException {

        for (String instrumentablePackageName : packagesToInstrument) {
            if (className.startsWith(instrumentablePackageName)) {
                return applyTransformations(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            }
        }

        return classfileBuffer;
    }

    private byte[] applyTransformations(
            final ClassLoader loader,
            final String className,
            final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain,
            final byte[] classfileBuffer) throws IllegalClassFormatException {


        requireNonNull(className, "Class name must not be null");
        
        final String javaClassName = className.replace('/', '.');
        
        LOG.trace("CFT0001247: Instrumenting class: {} ...", javaClassName);
        
        byte[] result = classfileBuffer;
        
        ClassPool cp = ClassPool.getDefault();
        
        CtClass clazz;
        try {
            clazz = cp.get(javaClassName);
        } catch (NotFoundException ex) {
            LOG.error("CFT0064585 Can not find class {}", javaClassName);
            return result;
        }

        for (ClassTransformer classTransformer : transformers) {
            if (classTransformer.shouldSkip(clazz)) {
                LOG.trace("CFT0011556: {} is skipping class {}", classTransformer.getName(), className);
                continue;
            }

            try {
                clazz = classTransformer.transform(clazz);
            } catch (Exception e) {
                LOG.error("CFT0013297 Failed to apply '{}' bytecode modifications to {} due to {}",
                        classTransformer.getName(), javaClassName, e.getMessage()
                );
            }
        }

        // Apply changes to bytecode if there are any
        if (clazz.isModified()) {
            try {
                result = clazz.toBytecode();
            } catch (IOException | CannotCompileException ex) {
                LOG.error("CFT0014824 Failed get bytecode of {} due to {}. The class remains untouched.", 
                        javaClassName, ex.getMessage()
                );
            }
        }

        return result;
    }

}
