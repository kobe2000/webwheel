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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * download result
 */
public class DownloadResult extends SimpleResult {

    protected InputStream stream;
    protected File file;
    protected boolean rangeSupport;
    protected String fileName;

    /**
     * @param file file in server
     * @param rangeSupport retry broken downloads support
     * @param fileName file name send to browser
     */
    public DownloadResult(File file, boolean rangeSupport, String fileName) {
        this.file = file;
        this.rangeSupport = rangeSupport;
        this.fileName = fileName;
    }

    /**
     * Constructor by input stream. This stream will be closed by framework.
     * @param stream input stream
     * @param fileName file name send to browser
     */
    public DownloadResult(InputStream stream, String fileName) {
        this.stream = stream;
        this.fileName = fileName;
    }

    protected void setResponseFileName() throws UnsupportedEncodingException {
        if (fileName != null) {
            String fn = URLEncoder.encode(fileName, "utf-8");
            ctx.getResponse().setHeader("Content-Disposition", String.format("attachment;filename=%s;filename*=UTF-8''%s", fn, fn));
        }
    }

    public void render() throws IOException {

        if (contentType != null) {
            ctx.getResponse().setContentType(contentType);
        }

        setResponseFileName();

        if (stream != null) {
            output(stream);
            return;
        }

        if (!file.exists() || !file.isFile()) {
            ctx.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String range = ctx.getRequest().getHeader("Range");
        if (range == null || !rangeSupport) {
            if (rangeSupport) {
                ctx.getResponse().setHeader("Accept-Ranges", "bytes");
            }
            ctx.getResponse().setContentLength((int) file.length());
            output(new FileInputStream(file));
            return;
        }

        int first, last;

        try {
            range = range.substring(6);
            int i = range.indexOf('-');
            if (i == 0) {
                last = (int) file.length() - 1;
                first = last - Integer.parseInt(range.substring(1));
                if (first < 0) first = 0;
            } else if (i == range.length() - 1) {
                first = Integer.parseInt(range.substring(0, range.length() - 1));
                last = (int) file.length() - 1;
            } else {
                first = Integer.parseInt(range.substring(0, i));
                last = Integer.parseInt(range.substring(i + 1));
            }
            if (last >= file.length()) last = (int) file.length() - 1;
            if (first < 0 || first > last) throw new Exception();
        } catch (Exception e) {
            ctx.getResponse().sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }

        ctx.getResponse().setContentLength(last - first + 1);
        ctx.getResponse().setHeader("Content-Range", "bytes " + first + "-" + last + "/" + file.length());
        ctx.getResponse().setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

        ServletOutputStream os = ctx.getResponse().getOutputStream();
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        try {
            raf.seek(first);
            byte[] buf = new byte[8192];
            do {
                int rd = raf.read(buf, 0, Math.min(buf.length, last - (int) raf.getFilePointer() + 1));
                os.write(buf, 0, rd);
            } while (raf.getFilePointer() <= last);
        } finally {
            raf.close();
        }
    }

    private void output(InputStream stream) throws IOException {
        try {
            ServletOutputStream os = ctx.getResponse().getOutputStream();
            byte[] buf = new byte[8192];
            int rd;
            while ((rd = stream.read(buf)) != -1) {
                os.write(buf, 0, rd);
            }
        } finally {
            stream.close();
        }
    }
}
