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

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import org.medal.clear.flow.agent.InstrumentationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisitClassTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(VisitClassTransformer.class);

    @Override
    public String getName() {
        return "VisitTransformer";
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

        final String javaClassName = cc.getName().replace('/', '.');
        final long calssId = (long) javaClassName.hashCode();
        updateClassDictionary(calssId, javaClassName);

        for (CtMethod method : cc.getDeclaredMethods()) {
            if (shouldSkip(method)) {
                continue;
            }

            try {

                String methodSignature = generateMethodSignature(method);
                long methodId = generateMethodId(javaClassName, methodSignature);
                updateSignatureDictionary(methodId, methodSignature);

                String entryCode = getMethodEntryCode(calssId, methodId);
                method.insertBefore(entryCode);

                String exitCode = getMethodExitCode();
                method.insertAfter(exitCode);

                // String failureCode = getFailureExitCode(method);
                // method.addCatch(exitCode, );
            } catch (CannotCompileException e) {
                throw new InstrumentationFailedException(e);
            }
        }

        return cc;
    }

    protected String getMethodEntryCode(long classCode, long messageCode) {
        // SequenceLogger.logEntry(long classCode, long instanceCode, long threadCode, long messageCode)
        StringBuilder cb = new StringBuilder();
        cb
                .append("{\n")
                .append("long instanceCode = System.identityHashCode($0);\n")
                .append("long threadCode = Thread.currentThread().getName().hashCode();\n")
                .append(CALL_LOGGER)
                .append(".logEntry(")
                .append(classCode).append("L").append(", ")
                .append("instanceCode").append(", ")
                .append(messageCode).append("L").append(", ")
                .append("threadCode")
                .append(");")
                .append("\n}");
        return cb.toString();
    }

    protected String getMethodExitCode() {
        StringBuilder cb = new StringBuilder();
        cb
                .append("{")
                .append(CALL_LOGGER).append(".logExit();")
                .append("}");
        return cb.toString();
    }
}
