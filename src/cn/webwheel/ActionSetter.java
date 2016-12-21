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

package cn.webwheel;

import cn.webwheel.setters.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thoughtworks.paranamer.*;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;

public class ActionSetter {

    private static final Logger logger = Logger.getLogger(ActionSetter.class.getName());

    private Map<Type, Setter> typeSetterMap = new HashMap<Type, Setter>();

    private Map<Class, List<SetterInfo>> setterMap = new HashMap<Class, List<SetterInfo>>();

    private Map<Method, List<SetterInfo>> argMap = new HashMap<Method, List<SetterInfo>>();

    private final String WRPName = "WebWheelRequestParams";

    private final static Paranamer paranamer = new AdaptiveParanamer(new DefaultParanamer(), new AnnotationParanamer(), new BytecodeReadingParanamer());

    @SuppressWarnings("unchecked")
    public Object[] set(Object action, ActionInfo ai, HttpServletRequest request) throws IOException {
        SetterConfig cfg = ai.getSetterConfig();
        List<SetterInfo> setters;
        if (action != null) {
            Class cls = action.getClass();
            setters = setterMap.get(cls);
            if (setters == null) {
                synchronized (this) {
                    setters = setterMap.get(cls);
                    if (setters == null) {
                        Map<Class, List<SetterInfo>> map = new HashMap<Class, List<SetterInfo>>(setterMap);
                        map.put(cls, setters = parseSetters(cls));
                        setterMap = map;
                    }
                }
            }
        } else {
            setters = Collections.emptyList();
        }

        List<SetterInfo> args = argMap.get(ai.actionMethod);
        if (args == null) {
            synchronized (this) {
                args = argMap.get(ai.actionMethod);
                if (args == null) {
                    Map<Method, List<SetterInfo>> map = new HashMap<Method, List<SetterInfo>>(argMap);
                    map.put(ai.actionMethod, args = parseArgs(ai.actionMethod));
                    argMap = map;
                }
            }
        }

        if (setters.isEmpty() && args.isEmpty()) return new Object[0];

        Map<String, Object> params;
        try {
            if (cfg.getCharset() != null) {
                request.setCharacterEncoding(cfg.getCharset());
            }
        } catch (UnsupportedEncodingException e) {
            //
        }

        if (ServletFileUpload.isMultipartContent(request)) {
            params = new HashMap<String, Object>(request.getParameterMap());
            request.setAttribute(WRPName, params);
            ServletFileUpload fileUpload = new ServletFileUpload();
            if (cfg.getCharset() != null) {
                fileUpload.setHeaderEncoding(cfg.getCharset());
            }
            if (cfg.getFileUploadSizeMax() != 0) {
                fileUpload.setSizeMax(cfg.getFileUploadSizeMax());
            }
            if (cfg.getFileUploadFileSizeMax() != 0) {
                fileUpload.setFileSizeMax(cfg.getFileUploadFileSizeMax());
            }
            boolean throwe = false;
            try {
                FileItemIterator it = fileUpload.getItemIterator(request);
                while (it.hasNext()) {
                    FileItemStream fis = it.next();
                    if (fis.isFormField()) {
                        String s = Streams.asString(fis.openStream(), cfg.getCharset());
                        Object o = params.get(fis.getFieldName());
                        if (o == null) {
                            params.put(fis.getFieldName(), new String[]{s});
                        } else if (o instanceof String[]) {
                            String[] ss = (String[]) o;
                            String[] nss = new String[ss.length + 1];
                            System.arraycopy(ss, 0, nss, 0, ss.length);
                            nss[ss.length] = s;
                            params.put(fis.getFieldName(), nss);
                        }
                    } else if (!fis.getName().isEmpty()) {
                        File tempFile;
                        try {
                            tempFile = File.createTempFile("wfu", null);
                        } catch (IOException e) {
                            throwe = true;
                            throw e;
                        }
                        FileExImpl fileEx = new FileExImpl(tempFile);
                        Object o = params.get(fis.getFieldName());
                        if (o == null) {
                            params.put(fis.getFieldName(), new FileEx[]{fileEx});
                        } else if (o instanceof FileEx[]) {
                            FileEx[] ss = (FileEx[]) o;
                            FileEx[] nss = new FileEx[ss.length + 1];
                            System.arraycopy(ss, 0, nss, 0, ss.length);
                            nss[ss.length] = fileEx;
                            params.put(fis.getFieldName(), nss);
                        }
                        Streams.copy(fis.openStream(), new FileOutputStream(fileEx.getFile()), true);
                        fileEx.fileName = fis.getName();
                        fileEx.contentType = fis.getContentType();
                    }
                }
            } catch (FileUploadException e) {
                if (action instanceof FileUploadExceptionAware) {
                    ((FileUploadExceptionAware) action).setFileUploadException(e);
                }
            } catch (IOException e) {
                if (throwe) {
                    throw e;
                }
            }
        } else {
            params = request.getParameterMap();
        }

        if (cfg.getSetterPolicy() == SetterPolicy.ParameterAndField || (cfg.getSetterPolicy() == SetterPolicy.Auto && args.isEmpty())) {
            for (SetterInfo si : setters) {
                si.setter.set(action, si.member, params, si.paramName);
            }
        }

        Object[] as = new Object[args.size()];
        for (int i = 0; i < as.length; i++) {
            SetterInfo si = args.get(i);
            as[i] = si.setter.set(action, null, params, si.paramName);
        }
        return as;
    }

    public static String isSetter(Method method) {
        if (Modifier.isStatic(method.getModifiers())) return null;
        String name = method.getName();
        if (!name.startsWith("set")) return null;
        if (name.length() < 4) return null;
        if (method.getParameterTypes().length != 1) return null;
        name = name.substring(3);
        if (name.length() > 1 && name.equals(name.toUpperCase())) return name;
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public static String isGetter(Method method) {
        String name = method.getName();
        if (!name.startsWith("get")) return null;
        if (name.length() < 4) return null;
        if (method.getParameterTypes().length != 0) return null;
        if (method.getReturnType() == Void.class) return null;
        name = name.substring(3);
        if (name.length() > 1 && name.equals(name.toUpperCase())) return name;
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    public SetterInfo getSetterInfo(Member member, String paramName) {
        SetterInfo si = new SetterInfo();
        si.member = member;
        si.paramName = paramName;
        JsonProperty jsp;
        Type type;
        Class cls;
        if (member instanceof Field) {
            jsp = ((Field) member).getAnnotation(JsonProperty.class);
            type = ((Field) member).getGenericType();
            cls = ((Field) member).getType();
        } else {
            jsp = ((Method) member).getAnnotation(JsonProperty.class);
            type = ((Method) member).getGenericParameterTypes()[0];
            cls = ((Method) member).getParameterTypes()[0];
        }
        if (jsp != null) {
            si.setter = new JSonSetter(type);
            return si;
        }
        si.setter = typeSetterMap.get(type);
        if (si.setter != null) {
            return si;
        }
        si.setter = ValueOfSetter.create(cls);
        if (si.setter != null) {
            return si;
        }
        si.setter = ValueOfArraySetter.create(cls);
        if (si.setter != null) {
            return si;
        }
        si.setter = BeanSetter.create(cls, this);
        if (si.setter != null) {
            return si;
        }
        return null;
    }

    private List<SetterInfo> parseArgs(Method method) {
        Annotation[][] as = method.getParameterAnnotations();
        if (as.length == 0) {
            return Collections.emptyList();
        }
        String[] names = paranamer.lookupParameterNames(method, false);
        if (names == null || names.length != as.length) names = new String[as.length];
        for (int i = 0; i < names.length; i++) {
            if (method.getParameterTypes()[i].isArray()) {
                if (names[i] != null) names[i] += "[]";
            }
            for (int j = 0; j < as[i].length; j++) {
                if (as[i][j] instanceof WebParam) {
                    String v = ((WebParam) as[i][j]).value();
                    if (!v.isEmpty()) {
                        names[i] = v;
                    }
                }
            }
            if (names[i] == null) {
                logger.severe("need WebParam at argument[" + i + "] in " + method);
            }
        }
        List<SetterInfo> list = new ArrayList<SetterInfo>();
        for (int i = 0; i < names.length; i++) {
            SetterInfo si = new SetterInfo();
            list.add(si);
            si.paramName = names[i];
            JsonProperty jsp = null;
            for (Annotation a : as[i]) {
                if (a instanceof JsonProperty) {
                    jsp = (JsonProperty) a;
                    break;
                }
            }
            if (jsp != null) {
                si.setter = new JSonSetter(method.getGenericParameterTypes()[i]);
                continue;
            }
            si.setter = typeSetterMap.get(method.getGenericParameterTypes()[i]);
            if (si.setter != null) {
                continue;
            }
            si.setter = BeanSetter.create(method.getParameterTypes()[i], this);
            if (si.setter != null) {
                continue;
            }
            logger.severe("can not find setter at argument[" + i + "] in " + method);
            si.setter = new DefaultValueSetter(method.getParameterTypes()[i]);
        }
        return list;
    }

    public List<SetterInfo> parseSetters(Class cls) {
        List<SetterInfo> list = new ArrayList<SetterInfo>();
        Field[] fields = cls.getFields();
        for (int i = fields.length - 1; i >= 0; i--) {
            Field field = fields[i];
            if (Modifier.isFinal(field.getModifiers())) continue;
            if (Modifier.isStatic(field.getModifiers())) continue;
            WebParam param = field.getAnnotation(WebParam.class);
            String name = field.getName();
            if (field.getType().isArray()) name += "[]";
            if (param != null && !param.value().isEmpty()) name = param.value();
            SetterInfo si = getSetterInfo(field, name);
            if (si == null) {
                if (param != null) {
                    logger.severe("wrong WebParam used at " + field);
                }
                continue;
            }
            list.add(si);
        }

        Method[] methods = cls.getMethods();
        List<Method> maybeGetters = new ArrayList<Method>();
        for (int i = methods.length - 1; i >= 0; i--) {
            Method method = methods[i];
            WebParam param = method.getAnnotation(WebParam.class);
            if (param == null) continue;
            String name = isSetter(method);
            if (name == null) {
                maybeGetters.add(method);
                continue;
            }
            if (method.getParameterTypes()[0].isArray()) {
                name += "[]";
            }
            if (!param.value().isEmpty()) {
                name = param.value();
            }
            SetterInfo si = getSetterInfo(method, name);
            if (si == null) {
                logger.severe("wrong WebParam used at " + method);
                continue;
            }
            list.add(si);
        }
        for (Method method : maybeGetters) {
            String name = isGetter(method);
            if (name == null) {
                logger.severe("wrong WebParam used at " + method);
                continue;
            }
            if (method.getReturnType().isArray()) {
                name += "[]";
            }
            WebParam param = method.getAnnotation(WebParam.class);
            if (!param.value().isEmpty()) {
                name = param.value();
            }
            BeanSetter beanSetter = BeanSetter.create(method.getReturnType(), this);
            if (beanSetter == null) {
                logger.severe("wrong WebParam used at " + method);
                continue;
            }
            boolean find = false;
            for (SetterInfo si : list) {
                if (!si.paramName.equals(name)) continue;
                if (!(si.setter instanceof BeanSetter)) continue;
                if (!(si.member instanceof Method)) continue;
                if (((Method) si.member).getParameterTypes().length != 1) continue;
                if (((Method) si.member).getParameterTypes()[0] != method.getReturnType()) continue;
                find = true;
                BeanSetter bs = (BeanSetter) si.setter;
                bs.setGetter(method);
                break;
            }
            if (find) {
                continue;
            }
            SetterInfo si = new SetterInfo();
            si.paramName = name;
            si.member = method;
            si.setter = beanSetter;
            beanSetter.setGetter(method);
            list.add(si);
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    public void clear(HttpServletRequest request) {
        Map<String, Object> map = (Map<String, Object>) request.getAttribute(WRPName);
        if (map != null) {
            for (Object o : map.values()) {
                if (o instanceof FileEx[]) {
                    for (FileEx fileEx : (FileEx[]) o) {
                        fileEx.destroy();
                    }
                }
            }
            request.removeAttribute(WRPName);
        }
    }

    public void addSetter(Type type, Setter setter) {
        if (typeSetterMap.containsKey(type)) {
            throw new IllegalArgumentException("duplicated type");
        }
        typeSetterMap.put(type, setter);
    }
}
