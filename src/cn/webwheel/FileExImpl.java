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

import java.io.*;
import java.util.logging.Logger;

class FileExImpl implements FileEx {

    private static Logger logger = Logger.getLogger(FileExImpl.class.getName());

    File file;
    String fileName;
    String contentType;

    FileInputStream fis;

    public FileExImpl(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getSimpleFileName() {
        return fileName.substring(Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\')) + 1);
    }

    public String getExtension() {
        String s = getSimpleFileName();
        int i = s.lastIndexOf('.');
        if (i == -1) return "";
        return s.substring(i);
    }

    @Override
    public InputStream getStream()  {
        try {
            if (fis == null) fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
        return fis;
    }

    @Override
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[8192];
        int rd;
        try {
            while ((rd = fis.read(data)) != -1) {
                baos.write(data, 0, rd);
            }
            return baos.toByteArray();
        } finally {
            fis.close();
        }
    }

    @Override
    public void destroy() {
        if (file == null) {
            return;
        }
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                logger.warning("can not close file:" + file);
            }
            fis = null;
        }
        if (file.exists()) {
            if (!file.delete()) {
                logger.warning("can not delete file:" + file);
            }
        }
        file = null;
    }

    @Override
    public String toString() {
        return fileName;
    }
}
