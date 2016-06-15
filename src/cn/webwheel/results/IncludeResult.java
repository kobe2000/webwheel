package cn.webwheel.results;

import javax.servlet.ServletException;
import java.io.IOException;

public class IncludeResult extends SimpleResult {

    private final String path;

    /**
     * @param path path to forward
     */
    public IncludeResult(String path) {
        if (path == null) throw new IllegalArgumentException();
        this.path = path;
    }

    public void render() throws IOException, ServletException {
        ctx.getRequest().getRequestDispatcher(path).include(ctx.getRequest(), ctx.getResponse());
    }

    public String getPath() {
        return path;
    }
}
