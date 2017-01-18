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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;

public class JSonSetter extends AbstractSetter<Object> {

    private JavaType type;

    public static ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper
                .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    public JSonSetter(Type javaType) {
        type = objectMapper.getTypeFactory().constructType(javaType);
    }

    @Override
    protected Object get(Object param) {
        if (!(param instanceof String[])) return null;
        String[] ss = (String[]) param;
        try {
            return objectMapper.readValue(ss[0], type);
        } catch (IOException e) {
            return null;
        }
    }
}
