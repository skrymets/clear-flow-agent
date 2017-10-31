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

import java.util.Map;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import org.medal.clear.flow.agent.InstrumentationFailedException;
import org.medal.clear.flow.agent.SequenceLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisitClassTransformer extends DefaultClassTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(VisitClassTransformer.class);

    public final String LOGGER_CLASS = SequenceLogger.class.getName();

    public final String LOGGER_PKG = SequenceLogger.class.getPackage().getName();

    @Override
    public String getName() {
        return "visit";
    }

    @Override
    public boolean shouldSkip(CtClass cc) {
        return super.shouldSkip(cc)
                || cc.isFrozen();
    }

    @Override
    public CtClass transform(final CtClass cc) throws InstrumentationFailedException {

        // ClassPool classPool = cc.getClassPool();
        // classPool.importPackage(LOGGER_PKG);

        for (CtConstructor constructor : cc.getConstructors()) {
            try {
                constructor.insertBefore("System.out.println($class.getName());");
            } catch (CannotCompileException e) {
                throw new InstrumentationFailedException(e);
            }
        }

        for (CtMethod method : cc.getDeclaredMethods()) {
            try {
                instrumentMethod(cc, method);
            } catch (CannotCompileException e) {
                throw new InstrumentationFailedException(e);
            }
        }

        return cc;
    }

    private void instrumentMethod(CtClass cc, CtMethod method) throws CannotCompileException {

        boolean skip = method.isEmpty()
                || Modifier.isAbstract(method.getModifiers())
                || Modifier.isStatic(method.getModifiers());
        //TODO: Process static methods

        if (skip) {
            return;
        }
        
        String methodSignature = generateMethodSignature(method);
        long messageCode = methodSignature.hashCode();
        updateSignatureDictionary(messageCode, methodSignature);

        String entryCode = getMethodEntryCode(messageCode);
        method.insertBefore(entryCode);

        String exitCode = getMethodExitCode();
        method.insertAfter(exitCode);

        // String failureCode = getFailureExitCode(method);
        // method.addCatch(exitCode, );
    }

    private String getMethodEntryCode(long messageCode) {
        StringBuilder cb = new StringBuilder();
        cb
                .append("{")
                .append(LOGGER_CLASS).append(".logEntry((long)System.identityHashCode($0), ").append(messageCode).append("L").append(");")
                .append("}");
        return cb.toString();
    }

    private String getMethodExitCode() {
        StringBuilder cb = new StringBuilder();
        cb
                .append("{")
                .append(LOGGER_CLASS).append(".logExit();")
                .append("}");
        return cb.toString();
    }

    private void updateSignatureDictionary(long messageCode, String signature) {

        Map<Long, String> signatures = SequenceLogger.getSignatureDictionaries();

        String existingSignature = signatures.putIfAbsent(messageCode, signature);
        if (existingSignature != null) {
            LOG.warn("Method signatures code clash! Existing: {} New: {}", existingSignature, signature);
        }
    }

    private String generateMethodSignature(CtMethod method) {

        return new StringBuilder()
                .append(method.getName())
                .append(method.getSignature())
                .toString();

    }

}
