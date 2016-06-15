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

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.Map;

public class MapSSetter extends AbstractSetter<Map<String, String>> {

    @Override
    public Map<String, String> set(Object instance, Member member, Map<String, Object> params, String paramName) {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof String[]) {
                map.put(entry.getKey(), ((String[]) entry.getValue())[0]);
            }
        }
        set(instance, member, map);
        return map;
    }
}
