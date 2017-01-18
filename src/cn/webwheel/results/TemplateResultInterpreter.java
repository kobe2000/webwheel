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

import cn.webwheel.ResultInterpreter;
import cn.webwheel.WebContext;
import cn.webwheel.template.FileVisitor;
import cn.webwheel.template.RendererDelegate;
import cn.webwheel.template.RendererFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Result interpreter for template result
 * @see TemplateResult
 */
public class TemplateResultInterpreter implements ResultInterpreter<TemplateResult>,RendererDelegate {

    protected Map<String, RendererInfo> rendererInfoMap = new ConcurrentHashMap<String, RendererInfo>();

    protected int cacheTTL = 5000;

    protected RendererFactory factory;

    protected String charset;

    protected String contentType;

    public TemplateResultInterpreter(File root, String charset) {
        this.charset = charset;
        factory = new RendererFactory(root, charset, new ObjectMapper(), this);
    }

    public int getCacheTTL() {
        return cacheTTL;
    }

    public void setCacheTTL(int cacheTTL) {
        this.cacheTTL = cacheTTL;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    private RendererInfo getRendererInfo(String path, String templateCharset) throws IOException {

        long now = System.currentTimeMillis();

        RendererInfo ri = rendererInfoMap.get(path);
        if (ri != null) {
            if (now - ri.lastCheckTime > cacheTTL) {
                long lm = 0;
                for (File file : ri.files) {
                    lm += file.lastModified();
                }
                if (lm != ri.lastModified) {
                    ri = null;
                }
            }
        }
        if (ri == null) {
            ri = new RendererInfo();
            final RendererInfo finalRi = ri;
            FileVisitor fv = new FileVisitor() {
                @Override
                public String read(File root, String file, String charset) throws IOException {
                    finalRi.files.add(new File(root, file));
                    return super.read(root, file, charset);
                }
            };
            ri.renderer = factory.create(path, fv, templateCharset);
            for (File file : ri.files) {
                ri.lastModified += file.lastModified();
            }
            ri.lastCheckTime = now;
            rendererInfoMap.put(path, ri);
        }

        return ri;
    }

    @Override
    public void interpret(TemplateResult result, WebContext ctx) throws IOException, ServletException {

        String path = result.getPath();
        if (path == null) {
            path = ctx.getPath();
        }

        RendererInfo ri = getRendererInfo(path, result.getTemplateCharset());

        {
            String contentType = result.getContentType();
            if (contentType == null) contentType = ri.renderer.contentType;
            if (contentType == null) contentType = this.contentType;
            if (contentType == null) contentType = "text/html";
            ctx.getResponse().setContentType(contentType);
        }
        {
            String charset = result.getRenderCharset();
            if (charset == null) charset = ri.renderer.charset;
            if (charset == null) charset = this.charset;
            if (charset == null) charset = "utf-8";
            ctx.getResponse().setCharacterEncoding(charset);
        }

        ri.renderer.render(ctx.getResponse().getWriter(), result.getCom());
    }

    @Override
    public boolean write(Writer writer, Object obj) throws IOException {
        if (obj instanceof TemplateResult) {
            TemplateResult tr = (TemplateResult) obj;
            if (tr.getPath() == null) return false;
            RendererInfo ri = getRendererInfo(tr.getPath(), tr.getTemplateCharset());
            ri.renderer.render(writer, tr.getCom());
            return true;
        }
        return false;
    }
}
