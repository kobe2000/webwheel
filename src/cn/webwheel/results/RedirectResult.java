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

package cn.webwheel.results;

import java.io.IOException;

/**
 * Result of http redirect.
 * @see javax.servlet.http.HttpServletResponse#sendRedirect(String)
 */
public class RedirectResult extends SimpleResult {

    private final String path;

    /**
     * @param path path to redirect
     */
    public RedirectResult(String path) {
        if (path == null || path.isEmpty()) path = "/";
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void render() throws IOException {
        String path = this.path;
        if (path.startsWith("/")) {
            String contextPath = ctx.getRequest().getContextPath();
            if (!contextPath.isEmpty()) {
                path = contextPath + path;
            }
        }
        ctx.getResponse().sendRedirect(path);
    }
}
