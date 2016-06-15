/*
 * Copyright 2012 XueSong Guo.
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

import cn.webwheel.Setter;

import java.lang.reflect.*;
import java.util.Map;

abstract public class AbstractSetter<T> implements Setter<T> {

    protected T def;

    protected void set(Object instance, Member member, T value) {
        if (member == null) return;
        try {
            if (member instanceof Field) {
                ((Field) member).set(instance, value);
            } else {
                ((Method) member).invoke(instance, value);
            }
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        }
    }

    protected T get(Object param) {
        return null;
    }

    @Override
    public T set(Object instance, Member member, Map<String, Object> params, String paramName) {
        if (paramName == null) return def;
        Object param = params.get(paramName);
        T t = get(param);
        if (t != null) {
            set(instance, member, t);
            return t;
        }
        return def;
    }
}
