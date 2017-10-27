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
package org.medal.clear.flow.agent.impl;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import org.medal.clear.flow.agent.InstrumentationFailedException;

public class VisitClassTransformer extends DefaultClassTransformer {

    @Override
    public String getName() {
        return "visit";
    }

    @Override
    public boolean shouldSkip(CtClass clazz) {
        return super.shouldSkip(clazz)
                || clazz.isInterface()
                || clazz.isFrozen();
    }

    @Override
    public CtClass transform(final CtClass clazz) throws InstrumentationFailedException {

        for (CtConstructor constructor : clazz.getConstructors()) {
            try {
                constructor.insertBefore("System.out.println($class.getName());");
            } catch (CannotCompileException e) {
                throw new InstrumentationFailedException(e);
            }
        }

        return clazz;
    }

}
