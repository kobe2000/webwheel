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

import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class ValueOfArraySetter extends AbstractSetter {

    private Method method;
    private Class cls;

    public ValueOfArraySetter(Method method) {
        this.method = method;
        cls = method.getReturnType();
    }

    @Override
    protected Object get(Object param) {
        if (param instanceof String[]) {
            String[] ss = (String[]) param;
            Object arr = Array.newInstance(cls, ss.length);
            for (int i = 0; i < ss.length; i++) {
                try {
                    Array.set(arr, i, method.invoke(null, ss[i]));
                } catch (Exception ignored) {
                }
            }
            return arr;
        }
        return null;
    }

    public static ValueOfArraySetter create(Class cls) {
        if (!cls.isArray()) return null;
        cls = cls.getComponentType();
        Method method = ValueOfSetter.getValueOfMethod(cls);
        if (method == null) return null;
        return new ValueOfArraySetter(method);
    }
}
