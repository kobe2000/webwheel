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

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Result sending javascript to browser. Optionally wrapped into html page
 */
public class JavascriptResult extends SimpleResult {

    protected boolean wrap;
    protected String scripts;

    /**
     * @param scripts Javascript code. For example: alert('hi')
     * @param wrap Whether to wrap into html page
     */
    public JavascriptResult(String scripts, boolean wrap) {
        this.scripts = scripts;
        this.wrap = wrap;
    }

    public void render() throws IOException {
        String ct = contentType;
        if (ct == null) {
            ct = wrap ? "text/html" : "application/javascript";
        }
        if(wrap) {
            ctx.getResponse().setHeader("Cache-Control", "no-cache");
            ctx.getResponse().setContentType(ct);
            ctx.getResponse().setCharacterEncoding(charset);
            PrintWriter pw = ctx.getResponse().getWriter();
            pw.write("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><meta http-equiv=\"Cache-Control\" content=\"no-cache\"/><head><script type=\"text/javascript\">");
            pw.write(scripts);
            pw.write("</script></head><body></body></html>");
        } else {
            ctx.getResponse().setHeader("Cache-Control", "no-cache");
            ctx.getResponse().setContentType(ct);
            ctx.getResponse().setCharacterEncoding(charset);
            ctx.getResponse().getWriter().write(scripts);
        }
    }
}
