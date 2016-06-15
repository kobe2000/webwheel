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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet filter as WebWheel application entrance. <br>
 * <b>example:</b>
 * <pre>&lt;web-app>
   &lt;filter>
     &lt;filter-name>WebWheel&lt;/filter-name>
     &lt;filter-class>cn.webwheel.WebWheelFilter&lt;/filter-class>
     &lt;init-param>
       &lt;param-name>main&lt;/param-name>
       &lt;param-value>com.my.WebMain&lt;/param-value>
     &lt;/init-param>
   &lt;/filter>
   &lt;filter-mapping>
     &lt;filter-name>WebWheel&lt;/filter-name>
     &lt;url-pattern>/*&lt;/url-pattern>
   &lt;/filter-mapping>
&lt;/web-app></pre>
 * com.my.WebMain inherited from {@link cn.webwheel.Main}, in which there's application initializing code.
 */
public class WebWheelFilter implements javax.servlet.Filter {

    protected Main main;

    /**
     * Parse "main" parameter, create an instance and initialize it.
     */
    @SuppressWarnings("unchecked")
    public void init(FilterConfig filterConfig) throws ServletException {
        String s = filterConfig.getInitParameter("main");
        if (s == null || (s = s.trim()).isEmpty()) {
            throw new ServletException("parameter \"main\" must be set with WebWheelFilter in web.xml");
        }
        Class<? extends Main> cls;
        try {
            cls = (Class<? extends Main>) Class.forName(s);
        } catch (ClassNotFoundException e) {
            throw new ServletException("can not find class: " + s, e);
        }
        if (!Main.class.isAssignableFrom(cls)) {
            throw new ServletException("main class \"" + s + "\" must inherit class cn.webwheel.Main");
        }
        try {
            main = cls.newInstance();
        } catch (Exception e) {
            throw new ServletException("can not create instance of " + cls, e);
        }
        main.servletContext = filterConfig.getServletContext();
        main.init();
    }

    /**
     * filter method
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        main.process((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    /**
     * destroy "main" instance
     */
    public void destroy() {
        main.destroy();
        main = null;
    }
}
