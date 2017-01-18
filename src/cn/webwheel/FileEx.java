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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Uploaded file wrapper
 */
public interface FileEx {
    /**
     * A temporary file, valid while action method executing, its name is random.
     */
    File getFile();

    /**
     * File's name from client
     */
    String getFileName();

    /**
     * Content type in http post
     */
    String getContentType();

    /**
     * A handy method to get file's simple name(without directory information)
     */
    String getSimpleFileName();

    /**
     * Get file name's extension part
     */
    String getExtension();

    /**
     * A handy method to get input stream(a file input stream).<br/>
     */
    InputStream getStream();

    /**
     * A handy method to get byte array of uploaded file.
     */
    byte[] toBytes() throws IOException;

    /**
     * Delete temporary file. Don't need to call this in practice.
     */
    void destroy();
}
