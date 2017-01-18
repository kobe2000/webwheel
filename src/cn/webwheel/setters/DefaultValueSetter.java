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

import cn.webwheel.Setter;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

public class DefaultValueSetter implements Setter<Object> {

    protected static final Map<Class, Object> defs = new HashMap<Class, Object>();
    static {
        defs.put(byte.class, Byte.valueOf((byte) 0));
        defs.put(boolean.class, Boolean.FALSE);
        defs.put(char.class, Character.valueOf('\0'));
        defs.put(short.class, Short.valueOf((short) 0));
        defs.put(int.class, Integer.valueOf(0));
        defs.put(long.class, Long.valueOf(0));
        defs.put(float.class, Float.valueOf(0));
        defs.put(double.class, Double.valueOf(0));
    }

    Object def;

    public DefaultValueSetter(Class type) {
        def = defs.get(type);
    }

    @Override
    public Object set(Object instance, Member member, Map<String, Object> params, String paramName) {
        return def;
    }
}
