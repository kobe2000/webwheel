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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MVC context information.
 */
public interface WebContext {

    /**
     * The http url(after http request forward).
     * @see Main#map(String)
     * @return http url
     */
    String getPath();

    /**
     * Get "main" instance.
     * @see WebWheelFilter
     * @return "main" instance
     */
    Main getMain();

    /**
     * @return javaEE servlet context
     */
    ServletContext getContext();

    /**
     * @return javaEE http servlet request
     */
    HttpServletRequest getRequest();

    /**
     * @return javaEE http servlet response
     */
    HttpServletResponse getResponse();

}
