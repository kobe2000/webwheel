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

import cn.webwheel.ActionSetter;
import cn.webwheel.SetterInfo;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanSetter extends AbstractSetter<Object> {

    private List<SetterInfo> setters = new ArrayList<SetterInfo>();
    private Class<?> type;

    private static Map<Class, BeanSetter> beanSetterMap = new HashMap<Class, BeanSetter>();

    private BeanSetter(Class<?> type, ActionSetter actionSetter) {
        beanSetterMap.put(type, this);
        this.type = type;
        Field[] fields = type.getFields();
        for (int i = fields.length - 1; i >= 0; i--) {
            Field field = fields[i];
            if (Modifier.isFinal(field.getModifiers())) continue;
            if (Modifier.isStatic(field.getModifiers())) continue;
            SetterInfo si = actionSetter.getSetterInfo(field, field.getName());
            if (si != null) {
                setters.add(si);
            }
        }
        Method[] methods = type.getMethods();
        for (int i = methods.length - 1; i >= 0; i--) {
            Method m = methods[i];
            String name = ActionSetter.isSetter(m);
            if (name == null) continue;
            if (m.getParameterTypes()[0].isArray()) name += "[]";
            SetterInfo si = actionSetter.getSetterInfo(m, name);
            if (si != null) {
                setters.add(si);
            }
        }
    }

    @Override
    public Object set(Object instance, Member member, Map<String, Object> params, String paramName) {
        Object bean = null;
        for (SetterInfo si : setters) {
            String name = paramName + '.' + si.paramName;
            if (!params.containsKey(name)) {
                continue;
            }
            if (bean == null) {
                try {
                    bean = type.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            si.setter.set(bean, si.member, params, paramName + '.' + si.paramName);
        }
        set(instance, member, bean);
        return bean;
    }

    public static BeanSetter create(Class cls, ActionSetter actionSetter) {
        BeanSetter bs = beanSetterMap.get(cls);
        if (bs != null) {
            return bs;
        }
        try {
            cls.getConstructor();
        } catch (NoSuchMethodException e) {
            return null;
        }
        BeanSetter si = new BeanSetter(cls, actionSetter);
        if (si.setters.isEmpty()) {
            beanSetterMap.remove(cls);
            return null;
        }
        return si;
    }
}
