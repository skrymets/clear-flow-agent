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
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.medal.clear.flow.agent.InstrumentationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvokeTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(InvokeTransformer.class);

    @Override
    public boolean shouldSkip(CtClass cc) {
        return super.shouldSkip(cc)
                || cc.getPackageName().startsWith("java/lang")
                || cc.getPackageName().startsWith("java/io")
                ;

    }

    @Override
    public String getName() {
        return InvokeTransformer.class.getSimpleName();
    }

    @Override
    public CtClass transform(CtClass cc) throws InstrumentationFailedException {

        final long thisCalssId = (long) cc.getName().hashCode();
        updateClassDictionary(thisCalssId, cc.getName());

        CtMethod logCallMethod;
        CtMethod logReturn;
        try {
            cc.addField(new CtField(CtClass.longType, "thisClassId", cc), CtField.Initializer.constant(thisCalssId));

            logCallMethod = new CtMethod(CtClass.voidType, "logCall",
                    new CtClass[]{
                        CtClass.longType, // target class id
                        CtClass.longType, // target method id
                    },
                    cc);
            cc.addMethod(logCallMethod);

            logReturn = new CtMethod(CtClass.voidType, "logReturn", new CtClass[]{CtClass.longType}, cc);
            cc.addMethod(logReturn);

        } catch (CannotCompileException ex) {
            LOG.error("IT00685156 Failed to instrument log mthod for {} due to: {}", cc.getName(), ex.getReason());
            return cc;
        }

        // *******************************************************************************
        // Run methods instrumentation
        // *******************************************************************************
        for (CtMethod method : cc.getDeclaredMethods()) {

            if (shouldSkip(method)) {
                continue;
            }

            String thisMethodSignature = generateMethodSignature(method);
            long thisMethodId = generateMethodId(cc.getName(), thisMethodSignature);
            updateSignatureDictionary(thisMethodId, thisMethodSignature);

            // ***************************************************************************
            // Log all the internal calls 
            // ***************************************************************************
            try {
                method.instrument(new ExprEditor() {
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {

                        String targetMethodSignature;
                        try {
                            targetMethodSignature = generateMethodSignature(m.getMethod());
                        } catch (NotFoundException ex) {
                            LOG.warn("TI00688109 Skip method's instrumentation {}.{}{}. Can't find any info about it.",
                                    m.getClassName(), m.getMethodName(), m.getSignature());
                            return;
                        }

                        long targetMethodId = generateMethodId(m.getClassName(), targetMethodSignature);
                        updateSignatureDictionary(targetMethodId, targetMethodSignature);

                        long targetCalssId = (long) m.getClassName().hashCode();
                        updateClassDictionary(targetCalssId, m.getClassName());

                        LOG.trace("{}: {}.{}{}", m.getLineNumber(), m.getClassName(), m.getMethodName(), m.getSignature());
                        m.replace("{ "
                                + "logCall("
                                + "(long)" + targetCalssId + ", "
                                + "(long)" + targetMethodId + "); "
                                + "$_ = $proceed($$); "
                                + "}");
                    }

//                    @Override
//                    public void edit(NewExpr e) throws CannotCompileException {
//                        LOG.trace("{}: {}.<init>{}", e.getLineNumber(), e.getClassName(), e.getSignature());
//                    }
                });
            } catch (CannotCompileException ex) {
                LOG.error("IT00682893 Failed to instrument {}.{}{} due to: {}",
                        cc.getName(), method.getName(), method.getSignature(), ex.getReason());
            }

            // ***************************************************************************
            // Log return from this method
            // ***************************************************************************
            try {
                method.insertAfter("{ logReturn((long) " + thisMethodId + "); }");
            } catch (CannotCompileException ex) {
                LOG.error("IT00682901 Failed to add log return code for  {}.{}{} due to: {}",
                        cc.getName(), method.getName(), method.getSignature(), ex.getReason());
            }

            // String failureCode = getFailureExitCode(method);
            // method.addCatch(exitCode, );
        }

        // *******************************************************************************
        // Add the method's body at the last stage to avoid it's instrumentation 
        // *******************************************************************************
        try {
            /**
             * public static void logMethodCall(
             * long sourceClassId,
             * long sourceInstanceId,
             * long targetClassId,
             * long targetMethodId,
             * long threadId
             * )
             */
            logCallMethod.setBody(
                    new StringBuilder().append("{\n")
                    .append("long instanceId = System.identityHashCode($0);\n")
                    .append("long threadId = Thread.currentThread().getName().hashCode();\n")
                    .append(CALL_LOGGER).append(".logMethodCall(")
                    .append("(long) $0.thisClassId").append(", ")
                    .append("instanceId").append(", ")
                    .append("(long) $1").append(", ")
                    .append("(long) $2").append(", ")
                    .append("threadId")
                    .append(");")
                    .append("}")
                    .toString()
            );

            /**
             * public static void logReturnFromMethod(
             * long classId,
             * long instanceId,
             * long methodId,
             * long threadId
             * )
             */
            logReturn.setBody(
                    new StringBuilder().append("{\n")
                    .append("long instanceId = System.identityHashCode($0);\n")
                    .append("long threadId = Thread.currentThread().getName().hashCode();\n")
                    .append(CALL_LOGGER).append(".logReturnFromMethod(")
                    .append("(long) $0.thisClassId").append(", ")
                    .append("instanceId").append(", ")
                    .append("(long) $1").append(", ")
                    .append("threadId")
                    .append(");")
                    .append("}")
                    .toString()
            );

            logCallMethod.setModifiers(logCallMethod.getModifiers() & ~Modifier.ABSTRACT);
            logReturn.setModifiers(logReturn.getModifiers() & ~Modifier.ABSTRACT);

            cc.setModifiers(cc.getModifiers() & ~Modifier.ABSTRACT);

        } catch (CannotCompileException ex) {
            LOG.error("IT00685157 Failed to instrument log mthod for {} due to: {}", cc.getName(), ex.getReason());
            return cc;
        }

        return cc;
    }

}
