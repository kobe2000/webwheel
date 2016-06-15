package cn.webwheel.template;

import java.io.IOException;
import java.io.Writer;

public interface RendererDelegate {
    boolean write(Writer writer, Object obj) throws IOException;
}
