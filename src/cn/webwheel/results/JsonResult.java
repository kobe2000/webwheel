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

import cn.webwheel.setters.JSonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Result of json object
 */
public class JsonResult extends SimpleResult {

    /**
     * Jackson ObjectMapper
     */
    public static ObjectMapper objectMapper = JSonSetter.objectMapper;

    /**
     * Whether to wrap into &lt;textarea> when http request is not ajax request
     */
    public static boolean defWrapMultipart = false;

    protected Object json;
    protected Map<String, Object> map;
    protected boolean wrapMultipart = defWrapMultipart;

    public JsonResult() {
        map = new HashMap<String, Object>();
    }

    public JsonResult(Object json) {
        this.json = json;
    }

    public JsonResult remove(String name) {
        if (map == null) throw new IllegalStateException("not use default constructor");
        map.remove(name);
        return this;
    }

    public JsonResult set(String name, Object value) {
        if (map == null) throw new IllegalStateException("not use default constructor");
        map.put(name, value);
        return this;
    }

    public Object get(String name) {
        if (map == null) throw new IllegalStateException("not use default constructor");
        return map.get(name);
    }

    public void render() throws IOException {
        ctx.getResponse().setCharacterEncoding(charset);
        String s = toString();
        if (wrapMultipart && ServletFileUpload.isMultipartContent(ctx.getRequest()) && !"XMLHttpRequest".equals(ctx.getRequest().getHeader("X-Requested-With"))) {
            ctx.getResponse().setContentType(contentType == null ? "text/html" : contentType);
            PrintWriter pw = ctx.getResponse().getWriter();
            final String s1 = "<textarea>";
            final String s2 = "</textarea>";
            pw.write(s1, 0, s1.length());
            pw.write(s);
            pw.write(s2, 0, s2.length());
            return;
        }
        String ct = contentType;
        if (ct == null) {
            ct = "application/json";
            String ua = ctx.getRequest().getHeader("User-Agent");
            if (ua != null && ua.contains("MSIE")) {
                ct = "text/plain;charset=" + charset;
            }
        }
        ctx.getResponse().setContentType(ct);
        ctx.getResponse().getWriter().write(s);
    }

    public String toString() {
        try {
            return objectMapper.writeValueAsString(json != null ? json : map);
        } catch (IOException e) {
            try {
                return "{\"msg\":" + objectMapper.writeValueAsString(e.toString()) + "}";
            } catch (IOException e1) {
                return e1.toString();
            }
        }
    }

    public boolean isWrapMultipart() {
        return wrapMultipart;
    }

    public void setWrapMultipart(boolean wrapMultipart) {
        this.wrapMultipart = wrapMultipart;
    }
}
