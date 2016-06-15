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
    private Method getter;

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
        for (int i = methods.length - 1; i >= 0; i--) {
            Method m = methods[i];
            String name = ActionSetter.isGetter(m);
            if (name == null) continue;
            if (m.getReturnType().isArray()) name += "[]";
            BeanSetter beanSetter = BeanSetter.create(m.getReturnType(), actionSetter);
            if (beanSetter == null) {
                continue;
            }
            boolean find = false;
            for (SetterInfo si : setters) {
                if (!si.paramName.equals(name)) continue;
                if (!(si.setter instanceof BeanSetter)) continue;
                if (!(si.member instanceof Method)) continue;
                if (((Method) si.member).getParameterTypes().length != 1) continue;
                if (((Method) si.member).getParameterTypes()[0] != m.getReturnType()) continue;
                find = true;
                BeanSetter bs = (BeanSetter) si.setter;
                bs.setGetter(m);
                break;
            }
            if (find) {
                continue;
            }
            SetterInfo si = new SetterInfo();
            si.paramName = name;
            si.member = m;
            si.setter = beanSetter;
            beanSetter.setGetter(m);
            setters.add(si);
        }
    }

    @Override
    public Object set(Object instance, Member member, Map<String, Object> params, String paramName) {
        Object bean = null;
        if (getter != null) {
            try {
                bean = getter.invoke(instance);
            } catch (IllegalAccessException ignored) {
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
            if (bean == null && member == getter) return null;
        }
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
        if (bean == null) {
            return null;
        }
        if (member != getter) {
            set(instance, member, bean);
        }
        return bean;
    }

    public void setGetter(Method getter) {
        this.getter = getter;
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
        if (si.setters.isEmpty()) return null;
        return si;
    }
}
