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

/**
 * Result to render html page through template
 * @see TemplateResultInterpreter
 */
public class TemplateResult {

    private String path;
    private Object com;

    private String templateCharset;
    private String renderCharset;
    private String contentType;

    /**
     * @param path template file path, relative to web root
     * @param com model object
     */
    public TemplateResult(Object com, String path) {
        this.path = path;
        this.com = com;
    }


    /**
     * default to use http request uri as template file path
     * @param com model object
     */
    @SuppressWarnings("unchecked")
    public TemplateResult(Object com) {
        this(com, null);
    }

    public String getPath() {
        return path;
    }

    public Object getCom() {
        return com;
    }

    public String getRenderCharset() {
        return renderCharset;
    }

    /**
     * @param renderCharset rendering charset
     */
    public void setRenderCharset(String renderCharset) {
        this.renderCharset = renderCharset;
    }

    public String getTemplateCharset() {
        return templateCharset;
    }

    public void setTemplateCharset(String templateCharset) {
        this.templateCharset = templateCharset;
    }

    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType rendering content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
