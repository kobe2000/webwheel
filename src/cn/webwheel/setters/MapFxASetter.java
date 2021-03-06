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

package cn.webwheel.setters;

import cn.webwheel.FileEx;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

public class MapFxASetter extends AbstractSetter<Map<String, FileEx[]>> {

    @Override
    public Map<String, FileEx[]> set(Object instance, Member member, Map<String, Object> params, String paramName) {
        Map<String, FileEx[]> map = new HashMap<String, FileEx[]>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof FileEx[]) {
                map.put(entry.getKey(), ((FileEx[]) entry.getValue()));
            }
        }
        set(instance, member, map);
        return map;
    }
}
