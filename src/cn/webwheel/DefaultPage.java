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

import cn.webwheel.results.RedirectResult;
import cn.webwheel.results.TemplateResult;

import java.util.Map;

abstract public class DefaultPage extends DefaultAction {

    @Action
    public Object html() throws Exception {
        return new TemplateResult(this, path());
    }

    public RedirectResult redirect() {
        return super.redirect(getClass());
    }

    public RedirectResult redirect(String anchor, Object... kvs) {
        return super.redirect(getClass(), anchor, kvs);
    }

    public RedirectResult redirect(String anchor, Map<String, Object> params) {
        return super.redirect(getClass(), anchor, params);
    }

    public String path() {
        return path(getClass());
    }
}
