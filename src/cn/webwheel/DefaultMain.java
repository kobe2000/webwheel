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

package cn.webwheel;

import cn.webwheel.results.*;
import cn.webwheel.setters.*;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default main class.<br/>
 * Handling exception occurred while action method executing.<br/>
 */
public class DefaultMain extends Main {

    /**
     * Wrap exception to a json object and return it to client.
     * <p>
     * <b>json object format:</b><br/>
     * <p><blockquote><pre>
     *     {
     *         "msg": "the exception's message",
     *         "stackTrace":[
     *             "exception's stack trace1",
     *             "exception's stack trace2",
     *             "exception's stack trace3",
     *             ....
     *         ]
     *     }
     * </pre></blockquote></p>
     */
    public Object executeActionError(WebContext ctx, ActionInfo ai, Object action, Throwable e) throws Throwable {
        if (e instanceof LogicException) {
            return ((LogicException) e).getResult();
        }
        Logger.getLogger(DefaultMain.class.getName()).log(Level.SEVERE, "action execution error", e);
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        String s;
        try {
            s = JsonResult.objectMapper.writeValueAsString(e.toString());
        } catch (IOException e1) {
            s = "\"" + e.toString().replace("\"", "'") + "\"";
        }
        sb.append("    \"msg\" : " + s + ",\n");
        sb.append("    \"stackTrace\" : [");
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String[] ss = sw.toString().split("\r\n");
        for (int i = 1; i < ss.length; i++) {
            if (sb.charAt(sb.length() - 1) != '[') {
                sb.append(',');
            }
            sb.append("\n        ").append(JsonResult.objectMapper.writeValueAsString(ss[i]));
        }
        sb.append("\n    ]\n");
        sb.append("}");
        HttpServletResponse response = ctx.getResponse();
        if (JsonResult.defWrapMultipart && ServletFileUpload.isMultipartContent(ctx.getRequest()) && !"XMLHttpRequest".equals(ctx.getRequest().getHeader("X-Requested-With"))) {
            response.setContentType("text/html");
            sb.insert(0, "<textarea>\n");
            sb.append("\n</textarea>");
        } else {
            response.setContentType("application/json");
        }
        response.setCharacterEncoding("utf-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "-1");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(sb.toString());
        return EmptyResult.inst;
    }

    /**
     * WebWheel MVC application initializing method.<br/>
     * The default implement configures 2 {@link ResultInterpreter}s and 28 {@link Setter}s.<br/>
     * <p>
     * <b>result interpreter</b>:<br/>
     * {@link TemplateResult} to {@link TemplateResultInterpreter}<br/>
     * {@link SimpleResult} to {@link SimpleResultInterpreter}<br/>
     * <br/>
     * <b>http parameter binding setter</b>:<br/>
     * <br/>
     * String<br/>
     * String[]<br/>
     * <br/>
     * boolean<br/>
     * Boolean<br/>
     * boolean[]<br/>
     * <br/>
     * int<br/>
     * Integer<br/>
     * int[]<br/>
     * <br/>
     * long<br/>
     * Long<br/>
     * long[]<br/>
     * <br/>
     * float<br/>
     * Float<br/>
     * float[]<br/>
     * <br/>
     * double<br/>
     * Double<br/>
     * double[]<br/>
     * <br/>
     * File<br/>
     * File[]<br/>
     * <br/>
     * FileEx<br/>
     * FileEx[]<br/>
     * <br/>
     * Map&lt;String, Object><br/>
     * Map&lt;String, String><br/>
     * Map&lt;String, String[]><br/>
     * Map&lt;String, File><br/>
     * Map&lt;String, File[]><br/>
     * Map&lt;String, FileEx><br/>
     * Map&lt;String, FileEx[]>
     * @see FileEx
     */
    @Override
    protected void init() {

        File root = new File(servletContext.getRealPath("/"));

        interpret(TemplateResult.class).by(new TemplateResultInterpreter(root, null));
        interpret(SimpleResult.class).by(new SimpleResultInterpreter());

        set(String.class).by(new StringSetter());
        set(String[].class).by(new StringArraySetter());

        set(boolean.class).by(new BooleanSetter(Boolean.FALSE));
        set(Boolean.class).by(new BooleanSetter(null));
        set(boolean[].class).by(new BooleanArraySetter());

        set(byte.class).by(new ByteSetter((byte) 0));
        set(Byte.class).by(new ByteSetter(null));
        set(byte[].class).by(new ByteArraySetter());

        set(char.class).by(new CharSetter('\0'));
        set(Character.class).by(new CharSetter(null));
        set(char[].class).by(new CharArraySetter());

        set(short.class).by(new ShortSetter((short) 0));
        set(Short.class).by(new ShortSetter(null));
        set(short[].class).by(new ShortArraySetter());

        set(int.class).by(new IntSetter(0));
        set(Integer.class).by(new IntSetter(null));
        set(int[].class).by(new IntArraySetter());

        set(long.class).by(new LongSetter(0L));
        set(Long.class).by(new LongSetter(null));
        set(long[].class).by(new LongArraySetter());

        set(float.class).by(new FloatSetter(0f));
        set(Float.class).by(new FloatSetter(null));
        set(float[].class).by(new FloatArraySetter());

        set(double.class).by(new DoubleSetter(0.0));
        set(Double.class).by(new DoubleSetter(null));
        set(double[].class).by(new DoubleArraySetter());

        set(Date.class).by(new DateSetter());
        set(Date[].class).by(new DateArraySetter());

        set(File.class).by(new FileSetter());
        set(File[].class).by(new FileArraySetter());

        set(FileEx.class).by(new FileExSetter());
        set(FileEx[].class).by(new FileExArraySetter());

        set(new TypeLiteral<Map<String, Object>>(){}.getType()).by(new MapOSetter());

        set(new TypeLiteral<Map<String, String>>(){}.getType()).by(new MapSSetter());
        set(new TypeLiteral<Map<String, String[]>>(){}.getType()).by(new MapSASetter());

        set(new TypeLiteral<Map<String, File>>(){}.getType()).by(new MapFSetter());
        set(new TypeLiteral<Map<String, File[]>>(){}.getType()).by(new MapFASetter());

        set(new TypeLiteral<Map<String, FileEx>>(){}.getType()).by(new MapFxSetter());
        set(new TypeLiteral<Map<String, FileEx[]>>(){}.getType()).by(new MapFxASetter());

        set(WebParams.class).by(new WebParamsSetter(actionSetter));
    }

    /**
     * If exception occurred, call {@link #executeActionError(WebContext, ActionInfo, Object, Throwable)}
     */
    @Override
    protected Object executeAction(WebContext ctx, ActionInfo ai, Object action) throws Throwable {
        try {
            return super.executeAction(ctx, ai, action);
        } catch (Throwable e) {
            return executeActionError(ctx, ai, action, e);
        }
    }

    /**
     * Find action method under certain package recursively.
     * <p>
     * Action method must be marked by {@link Action}(may be through parent class).<br/>
     * Url will be the package path under rootpkg.<br/>
     * <b>example</b><br/>
     * action class:
     * <p><blockquote><pre>
     *     package com.my.app.web.user;
     *     public class insert {
     *        {@code @}Action
     *         public Object act() {...}
     *     }
     * </pre></blockquote><p>
     * This action method will be mapped to url: /user/insert.act
     * @see #map(String)
     * @see Action
     * @param rootpkg action class package
     */
    @SuppressWarnings("deprecation")
    final protected void autoMap(String rootpkg) {
        DefaultAction.defPagePkg = rootpkg;
        try {
            Enumeration<URL> enm = getClass().getClassLoader().getResources(rootpkg.replace('.', '/'));
            while (enm.hasMoreElements()) {
                URL url = enm.nextElement();
                if (url.getProtocol().equals("file")) {
                    autoMap(rootpkg.replace('.', '/'), rootpkg, new File(URLDecoder.decode(url.getFile())));
                } else if (url.getProtocol().equals("jar")) {
                    String file = URLDecoder.decode(url.getFile());
                    String root = file.substring(file.lastIndexOf('!') + 2);
                    file = file.substring(0, file.length() - root.length() - 2);
                    URL jarurl = new URL(file);
                    if (jarurl.getProtocol().equals("file")) {
                        JarFile jarFile = new JarFile(URLDecoder.decode(jarurl.getFile()));
                        try {
                            Enumeration<JarEntry> entries = jarFile.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry entry = entries.nextElement();
                                String name = entry.getName();
                                if (!name.endsWith(".class")) continue;
                                if (!name.startsWith(root + '/')) continue;
                                name = name.substring(0, name.length() - 6);
                                name = name.replace('/', '.');
                                int i = name.lastIndexOf('.');
                                autoMap(root, name.substring(0, i), name.substring(i + 1));
                            }
                        } finally {
                            jarFile.close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void autoMap(String root, String pkg, File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            String fn = file.getName();
            if (file.isDirectory()) {
                autoMap(root, pkg + "." + fn, file);
            } else if (fn.endsWith(".class")) {
                autoMap(root, pkg, fn.substring(0, fn.length() - 6));
            }
        }
    }

    private void getActions(List<Action> list, Set<Class> set, Class cls, Method method) {
        if (cls == null || !set.add(cls)) return;
        for (Method m : cls.getDeclaredMethods()) {
            if (!m.getName().equals(method.getName())) continue;
            if (!Arrays.equals(m.getParameterTypes(), method.getParameterTypes())) continue;
            Action action = m.getAnnotation(Action.class);
            if (action != null) {
                list.add(action);
            }
            break;
        }
        for (Class i : cls.getInterfaces()) {
            getActions(list, set, i, method);
        }
        getActions(list, set, cls.getSuperclass(), method);
    }

    private Action getAction(Class cls, Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            return method.getAnnotation(Action.class);
        }
        ArrayList<Action> actions = new ArrayList<Action>();
        getActions(actions, new HashSet<Class>(), cls, method);
        if (actions.isEmpty()) return null;
        if (actions.get(0).disabled()) return null;
        if (actions.size() == 1) return actions.get(0);
        ActionImpl action = new ActionImpl();
        for (Action act : actions) {
            if (action.merge(act)) {
                return action;
            }
        }
        return action;
    }

    @SuppressWarnings("unchecked")
    private void autoMap(String root, String pkg, String name) {
        Class cls;
        try {
            cls = Class.forName(pkg + "." + name);
        } catch (ClassNotFoundException e) {
            return;
        }
        if (cls.isMemberClass() && !Modifier.isStatic(cls.getModifiers())) {
            return;
        }
        if (cls.isAnonymousClass() || cls.isLocalClass()
                || !Modifier.isPublic(cls.getModifiers())
                || Modifier.isAbstract(cls.getModifiers())) {
            return;
        }

        name = name.replace('$', '.');

        for (Method method : cls.getMethods()) {

            String pathPrefix = pkg.substring(root.length()).replace('.', '/') + '/';
            String path = pathPrefix + name + '.' + method.getName();

            Action action = getAction(cls, method);
            if (action == null) continue;

            if (!action.value().isEmpty()) {
                if (action.value().startsWith("?")) {
                    path = path + action.value();
                } else if (action.value().startsWith(".")) {
                    path = pathPrefix + name + action.value();
                } else if (!action.value().startsWith("/")) {
                    path = pathPrefix + action.value();
                } else {
                    path = action.value();
                }
            }
            ActionBinder binder = map(path);
            if (!action.rest().isEmpty()) {
                String rest = action.rest();
                if (!rest.startsWith("/")) rest = pathPrefix + rest;
                binder = binder.rest(rest);
            }
            SetterConfig cfg = binder.with(cls, method);
            if (!action.charset().isEmpty()) {
                cfg = cfg.setCharset(action.charset());
            }
            if (action.fileUploadFileSizeMax() != 0) {
                cfg = cfg.setFileUploadFileSizeMax(action.fileUploadFileSizeMax());
            }
            if (action.fileUploadSizeMax() != 0) {
                cfg.setFileUploadSizeMax(action.fileUploadSizeMax());
            }
            if (action.setterPolicy() != SetterPolicy.Auto) {
                cfg.setSetterPolicy(action.setterPolicy());
            }
        }
    }

    private static class ActionImpl implements Action {

        String map = "";
        String rest = "";
        String charset = "";
        int fileUploadSizeMax;
        int fileUploadFileSizeMax;
        SetterPolicy setterPolicy = SetterPolicy.Auto;

        boolean merge(Action act) {
            if (act.disabled()) return true;
            if (map.isEmpty()) {
                map = act.value();
            }
            if (rest.isEmpty()) {
                rest = act.rest();
            }
            if (charset.isEmpty()) {
                charset = act.charset();
            }
            if (fileUploadSizeMax == 0) {
                fileUploadSizeMax = act.fileUploadSizeMax();
            }
            if (fileUploadFileSizeMax == 0) {
                fileUploadFileSizeMax = act.fileUploadFileSizeMax();
            }
            if (setterPolicy == SetterPolicy.Auto) {
                setterPolicy = act.setterPolicy();
            }
            return !map.isEmpty() && !rest.isEmpty() && !charset.isEmpty() && fileUploadSizeMax != 0 && fileUploadFileSizeMax != 0 && setterPolicy != SetterPolicy.Auto;
        }

        @Override
        public boolean disabled() {
            return false;
        }

        @Override
        public String value() {
            return map;
        }

        @Override
        public String rest() {
            return rest;
        }

        @Override
        public String charset() {
            return charset;
        }

        @Override
        public int fileUploadSizeMax() {
            return fileUploadSizeMax;
        }

        @Override
        public int fileUploadFileSizeMax() {
            return fileUploadFileSizeMax;
        }

        @Override
        public SetterPolicy setterPolicy() {
            return setterPolicy;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Action.class;
        }
    }
}
