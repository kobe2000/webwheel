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

/**
 * Config for http parameter binding.<br/>
 * Including http parameter encoding and file upload size limit.(reference <a href="http://commons.apache.org/fileupload/">Commons FileUpload</a>)
 */
final public class SetterConfig {

    private String charset = "utf-8";
    private int fileUploadSizeMax;
    private int fileUploadFileSizeMax;
    private SetterPolicy setterPolicy = SetterPolicy.Auto;

    /**
     * Default constructor.<br/>
     * Use utf-8 charset. not use fileUploadSizeMax and fileUploadFileSizeMax
     */
    public SetterConfig() {
    }

    /**
     * Copy constructor
     */
    public SetterConfig(SetterConfig sc) {
        charset = sc.charset;
        fileUploadSizeMax = sc.fileUploadSizeMax;
        fileUploadFileSizeMax = sc.fileUploadFileSizeMax;
        setterPolicy = sc.setterPolicy;
    }

    public String getCharset() {
        return charset;
    }

    public SetterConfig setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public int getFileUploadSizeMax() {
        return fileUploadSizeMax;
    }

    public SetterConfig setFileUploadSizeMax(int fileUploadSizeMax) {
        this.fileUploadSizeMax = fileUploadSizeMax;
        return this;
    }

    public int getFileUploadFileSizeMax() {
        return fileUploadFileSizeMax;
    }

    public SetterConfig setFileUploadFileSizeMax(int fileUploadFileSizeMax) {
        this.fileUploadFileSizeMax = fileUploadFileSizeMax;
        return this;
    }

    public SetterPolicy getSetterPolicy() {
        return setterPolicy;
    }

    public SetterConfig setSetterPolicy(SetterPolicy setterPolicy) {
        if (setterPolicy == null) setterPolicy = SetterPolicy.Auto;
        this.setterPolicy = setterPolicy;
        return this;
    }
}
