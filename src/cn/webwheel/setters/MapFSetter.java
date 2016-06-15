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

import cn.webwheel.FileEx;

import java.io.File;
import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

public class MapFSetter extends AbstractSetter<Map<String, File>> {

    @Override
    public Map<String, File> set(Object instance, Member member, Map<String, Object> params, String paramName) {
        Map<String, File> map = new HashMap<String, File>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof FileEx[]) {
                map.put(entry.getKey(), ((FileEx[]) entry.getValue())[0].getFile());
            }
        }
        set(instance, member, map);
        return map;
    }
}
