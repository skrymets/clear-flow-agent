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
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.medal.clear.flow.agent.InstrumentationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvokeTransformer extends AbstractTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(InvokeTransformer.class);

    @Override
    public String getName() {
        return InvokeTransformer.class.getSimpleName();
    }

    @Override
    public CtClass transform(CtClass clazz) throws InstrumentationFailedException {
        for (CtMethod method : clazz.getDeclaredMethods()) {
            try {
                method.instrument(new ExprEditor() {
                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        LOG.info("{}: {}.{}{}", 
                                m.getLineNumber(), 
                                m.getClassName(), 
                                m.getMethodName(), 
                                m.getSignature()
                        );
                    }
                });

            } catch (CannotCompileException ex) {
                LOG.error(ex.getMessage());
            }
        }

        return clazz;
    }

}
