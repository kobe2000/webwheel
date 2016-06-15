package cn.webwheel.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mvel2.MVEL;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.*;

public class TemplateRenderer {

    enum OutputContext {PlainText, AttributeValue, Javascript, JavascriptString, HrefValue}

    ObjectMapper objectMapper;
    RendererDelegate delegate;

    List<Object> content;

    public String charset;
    public String contentType;

    TemplateRenderer(List<Object> content, ObjectMapper objectMapper, RendererDelegate delegate) {
        this.content = content;
        this.objectMapper = objectMapper;
        this.delegate = delegate;
    }

    public void render(Writer writer, Object ctx) throws IOException {
        write(writer, ctx, Collections.<String, Object>emptyMap(), content);
    }

    private void write(Writer writer, Object ctx, Map<String, Object> vars, List<Object> content) throws IOException {
        for (Object obj : content) {
            if (obj instanceof Var) {
                write(writer, (Var) obj, ctx, vars);
            } else if (obj instanceof Attr) {
                Attr attr = (Attr) obj;
                Object exp = attr.var.exp;
                Object v = MVEL.executeExpression(exp, ctx, vars);
                if (v != null && !v.equals(Boolean.FALSE)) {
                    writer.write(' ');
                    writer.write(attr.name);
                    writer.write("=\"");
                    if (!v.equals(Boolean.TRUE)) {
                        write(writer, attr.var, ctx, vars);
                    }
                    writer.write("\"");
                }
            } else if (obj instanceof Block) {
                Block block = (Block) obj;
                write(writer, ctx, vars, block.vars, block.content);
            } else {
                writer.write(obj.toString());
            }
        }
    }

    private void write(Writer writer, Object ctx, Map<String, Object> vars, LinkedHashMap<String, Var> varMap, List<Object> content) throws IOException {
        Iterator<Map.Entry<String,Var>> it = varMap.entrySet().iterator();
        Map.Entry<String, Var> entry = it.next();
        Object obj = MVEL.executeExpression(entry.getValue().exp, ctx, vars);
        Iterator iter = null;
        if (entry.getKey().isEmpty()) {
            if (obj == null || obj.equals(Boolean.FALSE)) return;
            if (!obj.equals(Boolean.TRUE)) {
                String s = obj.toString();
                if (!entry.getValue().raw) {
                    s = s.replace("<", "&lt;");
                }
                writer.write(s);
                return;
            }
        } else {
            if (!entry.getValue().raw) {
                if (obj == null) return;
                if (obj instanceof Iterator) {
                    iter = (Iterator) obj;
                } else if (obj instanceof Iterable) {
                    iter = ((Iterable) obj).iterator();
                } else if (obj.getClass().isArray()) {
                    iter = new ArrayIterator(obj);
                }
            }
        }
        LinkedHashMap<String, Var> nvarMap = null;
        if (it.hasNext()) {
            nvarMap = new LinkedHashMap<String, Var>();
            do {
                entry = it.next();
                nvarMap.put(entry.getKey(), entry.getValue());
            } while (it.hasNext());
        }
        HashMap<String, Object> nvars = vars == null ? new HashMap<String, Object>() : new HashMap<String, Object>(vars);
        if (iter == null) {
            nvars.put(entry.getKey(), obj);
            if (nvarMap != null) {
                write(writer, ctx, nvars, nvarMap, content);
            } else {
                write(writer, ctx, nvars, content);
            }
        } else {
            int idx = 0;
            while (iter.hasNext()) {
                nvars.put(entry.getKey() + "_idx", idx++);
                nvars.put(entry.getKey(), iter.next());
                if (nvarMap != null) {
                    write(writer, ctx, nvars, nvarMap, content);
                } else {
                    write(writer, ctx, nvars, content);
                }
            }
        }
    }

    private void write(Writer writer, Var var, Object ctx, Map<String, Object> vars) throws IOException {
        Object obj = MVEL.executeExpression(var.exp, ctx, vars);
        if (obj == null) return;
        if (delegate != null) {
            if(delegate.write(writer, obj)) return;
        }
        if (!var.raw) {
            switch (var.ctx) {
                case PlainText:
                    writer.write(obj.toString().replace("<", "&lt;"));
                    return;
                case AttributeValue:
                    writer.write(obj.toString().replace("\"", "&quot;"));
                    return;
                case Javascript:
                    writer.write(objectMapper.writeValueAsString(obj));
                    return;
                case JavascriptString:
                    String s = objectMapper.writeValueAsString(obj.toString());
                    writer.write(s.substring(1, s.length() - 1));
                    return;
                case HrefValue:
                    try {
                        writer.write(URLEncoder.encode(obj.toString(), "utf-8"));
                        return;
                    } catch (UnsupportedEncodingException ignored) {
                    }
                    break;
            }
        }
        writer.write(obj.toString());
    }

    private static class ArrayIterator implements Iterator {

        Object array;
        int i;
        int c;

        private ArrayIterator(Object array) {
            this.array = array;
            c = Array.getLength(array);
        }

        @Override
        public boolean hasNext() {
            return i < c;
        }

        @Override
        public Object next() {
            return Array.get(array, i++);
        }

        @Override
        public void remove() {
        }
    }

    static class Var {
        OutputContext ctx;
        Object exp;
        boolean raw;
    }

    static class Attr {
        String name;
        Var var;
    }

    static class Block {
        LinkedHashMap<String, Var> vars;
        List<Object> content;
    }
}
