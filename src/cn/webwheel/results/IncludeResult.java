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

import javax.servlet.ServletException;
import java.io.IOException;

public class IncludeResult extends SimpleResult {

    private final String path;

    /**
     * @param path path to forward
     */
    public IncludeResult(String path) {
        if (path == null) throw new IllegalArgumentException();
        this.path = path;
    }

    public void render() throws IOException, ServletException {
        ctx.getRequest().getRequestDispatcher(path).include(ctx.getRequest(), ctx.getResponse());
    }

    public String getPath() {
        return path;
    }
}
