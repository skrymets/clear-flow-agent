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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.medal.clear.flow.agent.transformers.InvokeTransformer;

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
                    // new VisitClassTransformer()
                    new InvokeTransformer()
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
                final String javaClassName = className.replace('/', '.');

                ClassPool cp = ClassPool.getDefault();
                CtClass clazz;
                try {
                    clazz = cp.get(javaClassName);
                } catch (NotFoundException ex) {
                    LOG.error("CFT0064585 Can not find class {}", javaClassName);
                    return null;
                }

                CtClass transformedClass = applyTransformations(clazz);

                // Apply changes to bytecode if there are any
                if (clazz.isModified()) {
                    byte[] bytecode;
                    try {
                        bytecode = transformedClass.toBytecode();
                    } catch (IOException | CannotCompileException ex) {
                        LOG.error("CFT0014824 Failed get bytecode of {} due to {}. The class remains untouched.", clazz.getName(), ex.getMessage());
                        return null;
                    }
                    transformedClass.detach();
                    return bytecode;
                }
            }
        }

        return classfileBuffer;
    }

    private CtClass applyTransformations(CtClass clazz) throws IllegalClassFormatException {

        LOG.trace("CFT0001247: Instrumenting class: {} ...", clazz.getName());

        for (ClassTransformer classTransformer : transformers) {
            if (classTransformer.shouldSkip(clazz)) {
                LOG.trace("CFT0011556: {} is skipping class {}", classTransformer.getName(), clazz.getName());
                continue;
            }

            try {
                classTransformer.transform(clazz);
            } catch (InstrumentationFailedException e) {
                LOG.error("CFT0013297 Failed to apply '{}' bytecode modifications to {} due to {}",
                        classTransformer.getName(), clazz.getName(), e.getMessage()
                );
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return clazz;
    }

}
