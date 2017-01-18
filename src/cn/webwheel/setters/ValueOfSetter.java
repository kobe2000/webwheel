/*
 * Copyright 2017 XueSong Guo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.webwheel.setters;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ValueOfSetter extends AbstractSetter {

    private Method method;

    public ValueOfSetter(Method method) {
        this.method = method;
    }

    @Override
    protected Object get(Object param) {
        if (param instanceof String[]) {
            String s = ((String[]) param)[0];
            try {
                return method.invoke(null, s);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static Method getValueOfMethod(Class cls) {
        for (Method method : cls.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            if (method.getReturnType() != cls) continue;
            Class<?>[] pts = method.getParameterTypes();
            if (pts.length != 1) continue;
            if (pts[0] != String.class) continue;
            return method;
        }
        return null;
    }

    public static ValueOfSetter create(Class cls) {
        Method method = getValueOfMethod(cls);
        if (method == null) return null;
        return new ValueOfSetter(method);
    }
}

