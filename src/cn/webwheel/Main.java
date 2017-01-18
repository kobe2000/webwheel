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

import cn.webwheel.setters.TypeLiteral;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Entrance of WebWheel MVC. There must be a class inherit this abstract class in each WebWheel application.<br>
 * Subclass must have default constructor(no argument), for being instantiated.
 */
abstract public class Main {

    /**
     * current servlet context
     */
    protected ServletContext servletContext;

    protected Map<Class, ResultInterpreter> interpreterMap = new HashMap<Class, ResultInterpreter>();

    protected Map<String, ActionInfo> actionMap = new HashMap<String, ActionInfo>();

    protected ActionSetter actionSetter = new ActionSetter();

    protected Map<Pattern, String> rewritePatterns = new HashMap<Pattern, String>();

    protected final SetterConfig defSetterConfig = new SetterConfig();

    /**
     * When action method is an instance method, action object is created by this method.
     * <p>
     * The default implement use {@link Class#newInstance()} to instantiate the action object, and set {@link WebContext} to it if action class inherited from {@link WebContextAware}.<br/>
     * Subclass can use IOC container to get action object.
     * @see WebContextAware
     * @see WebContext
     * @return action instance
     */
    public <T> T createAction(WebContext ctx, Class<T> type) {
        T action;
        try {
            action = type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("can not create instance of " + type, e);
        }
        if (action instanceof WebContextAware) {
            ((WebContextAware) action).setWebContext(ctx);
        }
        return action;
    }

    abstract protected void init();

    /**
     * Destroy method. To be invoked in {@link cn.webwheel.WebWheelFilter#destroy()}
     */
    protected void destroy() {}

    /**
     * According action result's type, find appropriate interpreter to interpret the result instance.
     * <p>
     * The finding procedure is from bottom to up in class inheritance diagram.
     * @param ctx current context
     * @param result result object of action method
     * @return interpreter is found
     * @throws IOException
     * @throws ServletException
     */
    @SuppressWarnings("unchecked")
    protected boolean interpretResult(WebContext ctx, Object result) throws IOException, ServletException {
        if (result == null) {
            return false;
        }

        Class cls = result.getClass();
        do {
            ResultInterpreter it = interpreterMap.get(cls);
            if (it != null) {
                it.interpret(result, ctx);
                return true;
            }
            for (Class inf : cls.getInterfaces()) {
                it = interpreterMap.get(inf);
                if (it != null) {
                    it.interpret(result, ctx);
                    return true;
                }
            }
        } while ((cls = cls.getSuperclass()) != null);

        return false;
    }

    /**
     * Invoke action method.
     * @param ctx current context
     * @param ai action info
     * @param action action object, may be null for static action method
     * @return result of action method
     */
    protected Object executeAction(WebContext ctx, ActionInfo ai, Object action) throws Throwable {
        try {
            Object[] args = actionSetter.set(action, ai, ctx.getRequest());
            try {
                return ai.actionMethod.invoke(action == null ? ai.actionMethod.getDeclaringClass() : action, args);
            } catch (IllegalAccessException ignored) {
                return null;
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        } finally {
            actionSetter.clear(ctx.getRequest());
        }
    }

    /**
     * Get http url(after http forward or include)
     */
    protected String pathFor(HttpServletRequest request) {
        String path = (String) request.getAttribute("javax.servlet.include.servlet_path");
        if (path != null) {
            String info = (String) request.getAttribute("javax.servlet.include.path_info");
            if (info != null) {
                path += info;
            }
        } else {
            path = request.getServletPath();
            String info = request.getPathInfo();
            if (info != null) {
                path += info;
            }
        }
        return path;
    }

    /**
     * Http request url rewriting.
     * @see ActionBinder#rest(String)
     * @param url http url
     * @return url after rewrite
     */
    protected String handleRewrite(String url) {
        for (Map.Entry<Pattern, String> entry : rewritePatterns.entrySet()) {
            Matcher matcher = entry.getKey().matcher(url);
            if (matcher.matches()) {
                String newurl = matcher.replaceAll(entry.getValue());
                if (!newurl.equals(url)) {
                    return newurl;
                }
            }
        }
        return null;
    }

    /**
     * http request handling procedure.
     */
    @SuppressWarnings("unchecked")
    protected void process(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String path = pathFor(request);

        String rewrite = handleRewrite(path);
        if (rewrite != null) {
            request.getRequestDispatcher(rewrite).forward(request, response);
            return;
        }

        ActionInfo ai = actionMap.get(path);
        if (ai == null) {
            filterChain.doFilter(request, response);
            return;
        }
        WebContextImpl ctx = new WebContextImpl(path, request, response);
        Object action = null;
        if (!Modifier.isStatic(ai.getActionMethod().getModifiers())) {
            action = createAction(ctx, ai.actionClass);
            if (action == null) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        Object result;
        try {
            if (ai.getActionMethod().getReturnType() == void.class) {
                executeAction(ctx, ai, action);
                return;
            }
            result = executeAction(ctx, ai, action);
        } catch (RuntimeException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (ServletException e) {
            throw e;
        } catch (Throwable e) {
            throw new ServletException(e);
        }

        boolean solved = interpretResult(ctx, result);

        if (!solved) {
            if (result == null) {
                filterChain.doFilter(request, response);
            } else {
                response.getWriter().write(result.toString());
            }
        }
    }

    /**
     * Map a http url to action method, optionally using url rewrite.
     * <p>
     * <b>example:</b><br/>
     * map("/index.html").with(PageIndex.class, "renderPage");
     * map("/article.html?id=$1").rest("/article/(\\d+)").with(Article.class, Article.class.getMethod("viewArticle"));
     * @see ActionBinder#rest(String)
     * @see ActionBinder#with(Class, String)
     * @see ActionBinder#with(Class, Method)
     * @param path http url
     * @return binder
     */
    protected ActionBinder map(String path) {
        return new ActionBinder(path);
    }

    protected class ActionBinder {

        protected String path;

        protected ActionBinder(String path) {
            this.path = path;
        }

        /**
         * Implement a simple url rewrite.
         * <p>
         * Using such procedure:<br/>
         * Pattern.compile(rest).matcher(url).replaceAll(path)
         * @param rest url pattern.
         * @return binder
         * @throws PatternSyntaxException rest pattern is wrong
         */
        public ActionBinder rest(String rest) throws PatternSyntaxException {
            Pattern pat = Pattern.compile(rest);
            for (Pattern p : rewritePatterns.keySet()) {
                if (p.pattern().equals(rest)) {
                    throw new IllegalArgumentException("duplicated rewrite path: " + rest);
                }
            }
            rewritePatterns.put(pat, path);
            return this;
        }

        /**
         * Map url to action method name.
         * <p>
         * There's must only one method named the parameter.
         * @param actionClass action class
         * @param methodName action method name
         * @return http parameter binding config
         */
        public SetterConfig with(Class actionClass, String methodName) throws NoSuchMethodException {
            Method m = null;
            for (Method mtd : actionClass.getMethods()) {
                if (mtd.getName().equals(methodName)) {
                    if (m != null) {
                        throw new NoSuchMethodException("duplicated method named: " + methodName + " in " + actionClass);
                    }
                    m = mtd;
                }
            }
            return with(actionClass, m);
        }

        /**
         * Map url to action method.
         * @param actionClass action class
         * @param method action method
         * @return http parameter binding config
         */
        @SuppressWarnings("unchecked")
        public SetterConfig with(Class actionClass, Method method) {
            if ((actionClass.isMemberClass() && !Modifier.isStatic(actionClass.getModifiers()))
                    || actionClass.isAnonymousClass() || actionClass.isLocalClass()
                    || !Modifier.isPublic(actionClass.getModifiers())
                    || Modifier.isAbstract(actionClass.getModifiers())) {
                throw new IllegalArgumentException("action class signature wrong: " + actionClass);
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException("action method signature wrong: " + method);
            }
            ActionInfo ai = new ActionInfo(actionClass, method);
            int i = path.indexOf('?');
            String realPath = i == -1 ? path : path.substring(0, i);
            if (actionMap.put(realPath, ai) != null) {
                throw new RuntimeException("duplicated action for path: " + realPath);
            }
            return ai.setterConfig = new SetterConfig(defSetterConfig);
        }
    }

    /**
     * Map a result type to result interpreter.
     * @param resultType result class type
     * @param <T> result class type
     * @return binder
     */
    protected <T> ResultTypeBinder<T> interpret(Class<T> resultType) {
        return new ResultTypeBinder<T>(resultType);
    }

    /**
     * Map a type to http parameter binding setter.
     * @param type parameter type, may be generic type.
     * @see TypeLiteral
     * @return binder
     */
    protected SetterBinder set(Type type) {
        return new SetterBinder(type);
    }

    protected class SetterBinder {

        protected Type type;

        protected SetterBinder(Type type) {
            this.type = type;
        }

        /**
         * map http parameter binding setter
         */
        public void by(Setter setter) {
            actionSetter.addSetter(type, setter);
        }
    }

    protected class ResultTypeBinder<T> {

        protected Class<T> resultType;

        protected ResultTypeBinder(Class<T> resultType) {
            this.resultType = resultType;
        }

        /**
         * map result interpreter
         */
        public void by(ResultInterpreter<? extends T> interpreterClass) {
            if (interpreterClass == null) {
                interpreterMap.remove(resultType);
            } else {
                interpreterMap.put(resultType, interpreterClass);
            }
        }
    }

    private class WebContextImpl implements WebContext {

        private String path;
        private HttpServletRequest request;
        private HttpServletResponse response;

        private WebContextImpl(String path, HttpServletRequest request, HttpServletResponse response) {
            this.path = path;
            this.request = request;
            this.response = response;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public Main getMain() {
            return Main.this;
        }

        @Override
        public ServletContext getContext() {
            return servletContext;
        }

        @Override
        public HttpServletRequest getRequest() {
            return request;
        }

        @Override
        public HttpServletResponse getResponse() {
            return response;
        }
    }
}
