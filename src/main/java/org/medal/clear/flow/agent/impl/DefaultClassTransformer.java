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

import javassist.CtClass;
import org.medal.clear.flow.agent.InstrumentationFailedException;
import org.medal.clear.flow.agent.ClassTransformer;

public class DefaultClassTransformer implements ClassTransformer {

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public boolean accepts(CtClass clazz) {
        return !shouldSkip(clazz);
    }

    @Override
    public boolean shouldSkip(CtClass clazz) {
        return clazz == null 
                || clazz.isPrimitive() 
                || clazz.isAnnotation() 
                || clazz.isArray();
    }

    @Override
    public CtClass transform(final CtClass clazz) throws InstrumentationFailedException {
        return clazz;
    }

}
