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

package cn.webwheel.results;

import cn.webwheel.ResultInterpreter;
import cn.webwheel.WebContext;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Result interpreter for simple result. Make results themselves control how to be interpreted.
 */
public class SimpleResultInterpreter implements ResultInterpreter<SimpleResult> {

    @Override
    public void interpret(SimpleResult result, WebContext ctx) throws IOException, ServletException {
        result.ctx = ctx;
        result.render();
    }
}
