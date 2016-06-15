package cn.webwheel.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mvel2.MVEL;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class RendererFactory {

    private ObjectMapper objectMapper;
    private RendererDelegate rendererDelegate;
    private File root;
    private String charset;

    private static final Set<String> hrefAttrs = new HashSet<String>(Arrays.asList(
            "a_href",
            "applet_code",
            "applet_archive",
            "applet_codebase",
            "area_href",
            "audio_src",
            "base_href",
            "blockquote_cite",
            "body_background",
            "button_formaction",
            "command_icon",
            "del_cite",
            "embed_src",
            "form_action",
            "frame_longdesc",
            "frame_src",
            "head_profile",
            "html_manifest",
            "iframe_longdesc",
            "iframe_src",
            "img_longdesc",
            "img_src",
            "input_formaction",
            "input_src",
            "ins_cite",
            "link_href",
            "object_archive",
            "object_codebase",
            "object_data",
            "q_cite",
            "script_src",
            "source_src",
            "track_src",
            "video_poster",
            "video_src"));

    public RendererFactory(File root, String charset, ObjectMapper objectMapper, RendererDelegate rendererDelegate) {
        this.root = root;
        this.charset = charset;
        this.objectMapper = objectMapper;
        this.rendererDelegate = rendererDelegate;
    }

    private List<PlainNode> read(FileVisitor visitor, String file, String charset) throws IOException {
        String temp = visitor.read(root, file, charset);
        return new PlainTemplateParser(temp).parse();
    }

    private boolean guessCharsetAndContentType(List<PlainNode> list, String[] charsetAndContentType) {
        for (PlainNode node : list) {
            if (node instanceof PlainElement) {
                PlainElement e = (PlainElement) node;
                if (e.getTag().equalsIgnoreCase("meta")) {
                    boolean isContentType = false;
                    String charset = null;
                    String contentType = null;
                    for (Map.Entry<String, String> entry : e.getAttributes().entrySet()) {
                        if (entry.getKey().equalsIgnoreCase("http-equiv")) {
                            isContentType = entry.getValue().trim().equalsIgnoreCase("content-type");
                        } else if (entry.getKey().equalsIgnoreCase("content")) {
                            String[] ss = entry.getValue().split(";");
                            contentType = ss[0].trim();
                            if (ss.length > 1) {
                                ss[1] = ss[1].trim();
                                if (ss[1].toLowerCase().startsWith("charset=")) {
                                    charset = ss[1].substring("charset=".length());
                                }
                            }
                        } else if (entry.getKey().equalsIgnoreCase("charset")) {
                            if (charsetAndContentType[0] == null) {
                                charsetAndContentType[0] = entry.getValue().trim();
                            }
                        }
                    }
                    if (isContentType) {
                        if (charsetAndContentType[1] == null) {
                            charsetAndContentType[1] = contentType;
                        }
                        if (charsetAndContentType[0] == null) {
                            charsetAndContentType[0] = charset;
                        }
                        if (charsetAndContentType[0] != null && charsetAndContentType[1] != null) {
                            return true;
                        }
                    }
                }
                if (e.getContent() != null) {
                    if (guessCharsetAndContentType(e.getContent(), charsetAndContentType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public TemplateRenderer create(String file, FileVisitor visitor, String charset) throws IOException {

        if (visitor == null) {
            visitor = new FileVisitor();
        }

        String[] charsetAndContentType = new String[2];

        if (charset == null) charset = this.charset;

        String ref = null;
        {
            int i = file.indexOf('#');
            if (i != -1) {
                ref = file.substring(i);
                file = file.substring(0, i);
            }
        }
        List<PlainNode> list;
        if (charset != null) {

            list = read(visitor, file, charset);

            replace(file, null, list, null, visitor, charset);

            guessCharsetAndContentType(list, charsetAndContentType);

            charsetAndContentType[0] = charset;

        } else {

            list = read(visitor, file, "utf-8");

            replace(file, null, list, null, visitor, "utf-8");

            guessCharsetAndContentType(list, charsetAndContentType);

            if (charsetAndContentType[0] != null && !Charset.forName(charsetAndContentType[0]).equals(Charset.forName("utf-8"))) {
                charset = charsetAndContentType[0];
                list = read(visitor, file, charset);
                replace(file, null, list, null, visitor, charset);
            }
        }

        if (ref != null) {
            Location loc = findLocation(visitor, file, ref, charset == null ? "utf-8" : charset);
            if (loc.e != null) {
                list = new ArrayList<PlainNode>();
                list.add(loc.e);
            } else {
                list = loc.list;
            }
        }

        List<Object> lst = translate(list, false);

        lst = zip(lst);

        TemplateRenderer renderer = new TemplateRenderer(lst, objectMapper, rendererDelegate);
        renderer.charset = charset;
        renderer.contentType = charsetAndContentType[1];
        return renderer;
    }

    private int[] indexOfExp(String text, int start) {
        int s, e;
        while (true) {
            for (s = start; s < text.length() - 3; s++) {
                char c = text.charAt(s);
                if (c == '$' || c == '#') {
                    if (text.charAt(s + 1) == '{') {
                        break;
                    }
                }
            }
            if (s >= text.length() - 3) {
                return null;
            }
            int pair = 1;
            for (e = s + 2; e < text.length(); e++) {
                char c = text.charAt(e);
                if (c == '\r' || c == '\n') {
                    break;
                }
                if (c == '{') {
                    pair++;
                } else if (c == '}') {
                    if (--pair == 0) {
                        return new int[]{s, e + 1};
                    }
                }
            }
            start = s + 2;
        }
    }

    private List<Object> zip(List<Object> list) {
        List<Object> lst = new ArrayList<Object>();
        StringBuilder sb = null;
        for (Object obj : list) {
            if (obj instanceof String) {
                if (sb == null) sb = new StringBuilder();
                sb.append(obj);
            } else {
                if (sb != null) lst.add(sb.toString());
                sb = null;
                lst.add(obj);
                if (obj instanceof TemplateRenderer.Block) {
                    ((TemplateRenderer.Block) obj).content = zip(((TemplateRenderer.Block) obj).content);
                }
            }
        }
        if (sb != null) lst.add(sb.toString());
        return lst;
    }

    private List<Object> translate(String text, TemplateRenderer.OutputContext ctx) {
        if (text == null) return null;
        List<Object> list = new ArrayList<Object>();
        int lastend = 0;
        while (true) {
            int[] se = indexOfExp(text, lastend);
            if (se == null) {
                break;
            }
            if (se[0] > lastend) {
                list.add(text.substring(lastend, se[0]));
            }
            lastend = se[1];

            TemplateRenderer.Var var = new TemplateRenderer.Var();
            var.ctx = ctx;
            var.raw = text.charAt(se[0]) == '#';
            var.exp = MVEL.compileExpression(text.substring(se[0] + 2, se[1] - 1));
            if (ctx == TemplateRenderer.OutputContext.Javascript && se[0] > 0 && se[1] < text.length()) {
                if ((text.charAt(se[0] - 1) == '"' || text.charAt(se[0] - 1) == '\'') || (text.charAt(se[1]) == '"' || text.charAt(se[1]) == '\'')) {
                    var.ctx = TemplateRenderer.OutputContext.JavascriptString;
                }
            }
            list.add(var);
        }
        if (lastend < text.length()) {
            list.add(text.substring(lastend));
        }
        return list;
    }

    private List<Object> translate(List<PlainNode> list, boolean inJs) {
        List<Object> lst = new ArrayList<Object>();
        for (PlainNode n : list) {
            if (n instanceof PlainText) {
                lst.addAll(translate(n.toString(), inJs ? TemplateRenderer.OutputContext.Javascript : TemplateRenderer.OutputContext.PlainText));
            } else {
                PlainElement e = (PlainElement) n;
                TemplateRenderer.Block block = new TemplateRenderer.Block();
                block.vars = new LinkedHashMap<String, TemplateRenderer.Var>();
                Iterator<Map.Entry<String, String>> it = e.getAttributes().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> entry = it.next();
                    if (entry.getKey().startsWith("t:")) {
                        it.remove();
                    } else if (entry.getKey().startsWith("w:")) {
                        TemplateRenderer.Var var = new TemplateRenderer.Var();
                        String v = entry.getValue().trim();
                        var.raw = v.startsWith("#{") && v.endsWith("}");
                        if ((v.startsWith("${") || v.startsWith("#{")) && v.endsWith("}")) {
                            v = v.substring(2, v.length() - 1);
                        }
                        try {
                            var.exp = MVEL.compileExpression(v);
                        } catch (Exception e1) {
                            continue;
                        }
                        block.vars.put(entry.getKey().substring(2), var);
                        it.remove();
                    }
                }
                List<Object> lst2 = lst;
                if (!block.vars.isEmpty()) {
                    lst.add(block);
                    block.content = new ArrayList<Object>();
                    lst2 = block.content;
                }
                boolean nt = e.getTag().toLowerCase().equals("nt");
                if (!nt) {
                    lst2.add("<" + e.getTag());
                    it = e.getAttributes().entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, String> entry = it.next();
                        TemplateRenderer.OutputContext ctx = TemplateRenderer.OutputContext.AttributeValue;
                        if (hrefAttrs.contains(e.getTag().toLowerCase() + "_" + entry.getKey().toLowerCase())) {
                            ctx = TemplateRenderer.OutputContext.HrefValue;
                        } else if (entry.getKey().toLowerCase().startsWith("on")) {
                            ctx = TemplateRenderer.OutputContext.Javascript;
                        }
                        List<Object> ts = null;
                        if (entry.getValue() != null) {
                            ts = translate(entry.getValue().trim(), ctx);
                        }
                        if (ts == null || ts.isEmpty()) {
                            lst2.add(" " + entry.getKey());
                            if (entry.getValue() != null) {
                                lst2.add("=\"" + entry.getValue() + "\"");
                            }
                        } else {
                            if (ts.size() == 1 && ts.get(0) instanceof TemplateRenderer.Var && !((TemplateRenderer.Var) ts.get(0)).raw) {
                                TemplateRenderer.Attr attr = new TemplateRenderer.Attr();
                                attr.var = (TemplateRenderer.Var) ts.get(0);
                                attr.name = entry.getKey();
                                lst2.add(attr);
                            } else {
                                lst2.add(" " + entry.getKey() + "=\"");
                                lst2.addAll(ts);
                                lst2.add("\"");
                            }
                        }
                    }
                    if (e.getContent() != null) {
                        lst2.add(">");
                    }
                }
                if (e.getContent() != null) {
                    lst2.addAll(translate(e.getContent(), e.getTag().toLowerCase().equals("script")));
                }
                if (!nt) {
                    if (e.getContent() == null) {
                        if (e.isClosed()) {
                            lst2.add("/>");
                        } else {
                            lst2.add(">");
                        }
                    } else {
                        lst2.add("</" + e.getTag() + ">");
                    }
                }
            }
        }
        return lst;
    }

    private String[] getSlot(PlainElement e) {
        String[] ss = new String[2];
        ss[0] = e.getAttributes().get("t:slot");
        ss[1] = e.getAttributes().get("t:ref");
        if (ss[0] == null && ss[1] == null) return null;
        if (ss[0] != null) ss[0] = ss[0].trim();
        if (ss[1] != null) ss[1] = ss[1].trim();
        return ss;
    }

    private List<PlainElement> getChildren(List<PlainNode> list) {
        List<PlainElement> lst = new ArrayList<PlainElement>();
        if (list != null) {
            for (PlainNode node : list) {
                if (node instanceof PlainElement) {
                    lst.add((PlainElement) node);
                }
            }
        }
        return lst;
    }

    private Map<String, PlainElement> getLocations(List<PlainNode> list, Map<String, PlainElement> map) {
        if (map == null) map = new HashMap<String, PlainElement>();
        for (PlainElement e : getChildren(list)) {
            String[] slot = getSlot(e);
            if (slot != null && slot[0] != null) {
                map.put(slot[0], e);
                continue;
            }
            if (e.getContent() != null) {
                getLocations(e.getContent(), map);
            }
        }
        return map;
    }

    static class BlockContext {
        String file;
        Map<String, PlainElement> slots = new HashMap<String, PlainElement>();
        BlockContext parent;
    }

    static class Location {
        String file;
        List<PlainNode> list;
        PlainElement e;
    }

    private Location findLocation(FileVisitor visitor, String currentFile, String ref, String charset) throws IOException {
        String[] ss = ref.split("#", -1);
        String f = visitor.getRef(currentFile, ss[0].trim());
        Location location = new Location();
        location.file = f;
        List<PlainNode> lst;
        try {
            lst = read(visitor, f, charset);
        } catch (IOException e) {
            throw new IOException("can not read reference: " + ref + ". path: " + f);
        }
        if (ss.length == 1) {
            location.list = lst;
            return location;
        }
        for (int i = 1; ; i++) {
            ss[i] = ss[i].trim();
            if (ss[i].isEmpty()) {
                location.list = lst;
                return location;
            }
            PlainElement pe = getLocations(lst, null).get(ss[i]);
            if (pe == null) {
                throw new FileNotFoundException("can not find reference: " + ref + ". path: " + f);
            }
            if (i == ss.length - 1) {
                location.e = pe;
                return location;
            }
            lst = pe.getContent();
        }
    }

    private void replace(String file, String ref, List<PlainNode> list, BlockContext ctx, FileVisitor visitor, String charset) throws IOException {
        if (list == null || list.isEmpty()) return;
        for (int i = 0; i < list.size(); i++) {
            PlainNode node = list.get(i);
            if (!(node instanceof PlainElement)) {
                continue;
            }
            PlainElement e = (PlainElement) node;
            String[] slot = getSlot(e);
            if (slot == null) {
                replace(file, ref, e.getContent(), ctx, visitor, charset);
                continue;
            }
            if (ctx != null && ctx.slots.containsKey(slot[0])) {
                PlainElement pe = ctx.slots.get(slot[0]);
                for (Map.Entry<String, String> entry : e.getAttributes().entrySet()) {
                    if (!pe.getAttributes().containsKey(entry.getKey())) {
                        pe.getAttributes().put(entry.getKey(), entry.getValue());
                    }
                }
                if ("".equals(pe.getAttributes().get("t:ref"))) {
                    pe.getAttributes().put("t:ref", ref + "#" + slot[0]);
                }
                List<PlainNode> lst = new ArrayList<PlainNode>();
                lst.add(pe);
                replace(ctx.file, ref, lst, ctx.parent, visitor, charset);
                list.remove(i);
                list.addAll(i, lst);
                i += lst.size() - 1;
                continue;
            }
            if (slot[1] == null) {
                replace(file, ref, e.getContent(), ctx, visitor, charset);
                continue;
            }
            BlockContext nctx = new BlockContext();
            nctx.parent = ctx;
            nctx.file = file;
            nctx.slots = getLocations(e.getContent(), null);
            Location loc = findLocation(visitor, file, slot[1], charset);

            PlainElement parent = loc.e;
            if (loc.list != null) {
                replace(loc.file, slot[1], loc.list, nctx, visitor, charset);
                parent = new PlainElement("nt", e.getLocation());
                parent.getContent().addAll(loc.list);
            } else {
                replace(loc.file, slot[1], Arrays.asList((PlainNode) parent), nctx, visitor, charset);
            }
            for (Map.Entry<String, String> entry : e.getAttributes().entrySet()) {
                if (!entry.getKey().startsWith("t:")) {
                    parent.getAttributes().put(entry.getKey(), entry.getValue());
                }
            }
            list.set(i, parent);
        }
    }
}
