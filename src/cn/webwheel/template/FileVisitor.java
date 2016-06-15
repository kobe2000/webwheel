package cn.webwheel.template;

import java.io.*;

public class FileVisitor {

    public String read(File root, String file, String charset) throws IOException {
        char[] buf = new char[8192];
        int rd;
        StringBuilder sb = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(new File(root, file))), charset);
        try {
            while ((rd = reader.read(buf)) != -1) {
                sb.append(buf, 0, rd);
            }
        } finally {
            reader.close();
        }
        return sb.toString();
    }

    public String getRef(String current, String ref) throws IOException {
        if (ref.isEmpty()) return current;
        if (ref.startsWith("/")) {
            return ref.substring(1);
        } else {
            current = current.replace("\\", "/");
            int i = current.lastIndexOf('/');
            if (i == -1) return ref;
            return current.substring(0, i + 1) + ref;
        }
    }
}
