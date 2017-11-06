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
package org.medal.clear.flow.agent.transformers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import org.medal.clear.flow.agent.ClassTransformer;
import org.medal.clear.flow.agent.InstrumentationFailedException;
import org.medal.clear.flow.agent.SequenceLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTransformer implements ClassTransformer {

    protected static final List<String> platformPackages = Arrays.asList(
            "java/applet", "java/awt", "java/beans", "java/io", "java/lang", "java/math", "java/net",
            "java/nio", "java/rmi", "java/security", "java/sql", "java/text", "java/time", "java/util"
    );

    public static boolean isPlatformClass(CtClass clazz) {
        if (clazz == null) {
            return false;
        } else {
            return (platformPackages.stream().filter((p) -> {
                return clazz.getName().startsWith(p);
            }).count() > 0);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ClassTransformer.class);

    protected final String CALL_LOGGER = SequenceLogger.class.getName();

    protected final String CALL_LOGGER_PKG = SequenceLogger.class.getPackage().getName();

    @Override
    public abstract String getName();

    @Override
    public abstract CtClass transform(final CtClass clazz) throws InstrumentationFailedException;

    @Override
    public boolean accepts(CtClass cc) {
        return !shouldSkip(cc);
    }

    @Override
    public boolean shouldSkip(CtClass cc) {
        return cc == null
                || cc.isPrimitive()
                || cc.isAnnotation()
                || cc.isInterface()
                || cc.isEnum()
                || cc.isArray()
                || cc.isFrozen();
    }

    @Override
    public boolean accepts(CtMethod method) {
        return !shouldSkip(method);
    }

    @Override
    public boolean shouldSkip(CtMethod method) {
        //TODO: Process static methods
        return method.isEmpty()
                || Modifier.isAbstract(method.getModifiers())
                || Modifier.isStatic(method.getModifiers());
    }

    protected String generateMethodSignature(CtMethod method) {
        return new StringBuilder()
                .append(method.getName())
                .append(method.getSignature())
                .toString();
    }

    protected void updateClassDictionary(long classCode, String javaClassName, boolean uniqueClassName) {
        Map<Long, String> classDictionary = SequenceLogger.getClassDictionary();
        String existingClassName = classDictionary.putIfAbsent(classCode, javaClassName);
        if (existingClassName != null && uniqueClassName) {
            LOG.warn("AA00682945 Class name code clash! Existing: {} New: {}",
                    existingClassName,
                    javaClassName
            );
        }
    }

    protected void updateSignatureDictionary(long messageCode, String signature, boolean uniqueSignature) {
        Map<Long, String> signatures = SequenceLogger.getSignatureDictionary();

        String existingSignature = signatures.putIfAbsent(messageCode, signature);
        if (existingSignature != null && uniqueSignature) {
            LOG.warn("AT00682943 Method signatures code clash! Existing: {} New: {}",
                    existingSignature,
                    signature);
        }
    }

    protected long generateMethodId(String javaClassName, String methodSignature) {
        return (long) new StringBuilder(javaClassName).append(".").append(methodSignature).toString().hashCode();
    }

}
