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

package cn.webwheel.template;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板解析器。
 */
public class PlainTemplateParser {

    private String string;
    private int stringIdx;

    private Stack<PlainElement> stack;

    private final static Pattern patForTag = Pattern.compile("<");
    private final static Pattern patForTagClose = Pattern.compile(">");
    private final static Pattern patForCmtClose = Pattern.compile("-->");
    private final static Pattern patForName = Pattern.compile("^\\s*[a-zA-Z_][a-zA-Z_0-9\\-:\\.]*");
    private final static Pattern patForEqual = Pattern.compile("^\\s*=\\s*");
    private final static Pattern patForCddClose = Pattern.compile("]]>");
    private final static Pattern patForNotBlank = Pattern.compile("\\S");
    private final static Pattern patForAttrValue = Pattern.compile("(^\".*?\")|(^'.*?')|(^[^\\s>]*)", Pattern.DOTALL);
    private final static Pattern patForScriptClose = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);

    private final static Set<String> autoClosedTags = new HashSet<String>();
    static {
        autoClosedTags.addAll(Arrays.asList("area", "base", "br", "col", "command", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"));
    }

    private StringBuilder text;

    public PlainTemplateParser(String string) {
        this.string = string;
    }

    public List<PlainNode> parse() throws TemplateParserException {
        text = new StringBuilder();
        stack = new Stack<PlainElement>();
        stack.push(new PlainElement(null, 0));
        while (stringIdx < string.length()) {
            Matcher matcher = patForTag.matcher(string.subSequence(stringIdx, string.length()));
            if (matcher.find()) {
                int start = matcher.start();
                text.append(string.subSequence(stringIdx, stringIdx + start));
                increaseStringIndex(start);
                parseTag();
            } else {
                text.append(string.subSequence(stringIdx, string.length()));
                stringIdx = string.length();
            }
        }
        if (stack.size() != 1) throw new TemplateParserException(string, string.length(), "tag " + stack.get(1).getTag() + " not close");
        flush();
        return stack.get(0).getContent();
    }

    private void parseTag() throws TemplateParserException {
        if (string.length() - stringIdx < 4) {
            throw new TemplateParserException(string, stringIdx, "tag error");
        }
        Pattern patForClose = null;
        if (string.subSequence(stringIdx, stringIdx + 4).toString().equals("<!--")) {
            patForClose = patForCmtClose;
        } else if (string.length() - stringIdx > 9 && string.subSequence(stringIdx, stringIdx + 9).toString().equals("<![CDATA[")) {
            patForClose = patForCddClose;
        } else if (string.charAt(stringIdx + 1) == '?' || string.charAt(stringIdx + 1) == '!') {
            patForClose = patForTagClose;
        }
        if (patForClose != null) {
            Matcher matcher = patForClose.matcher(string.subSequence(stringIdx, string.length()));
            if (!matcher.find()) throw new TemplateParserException(string, string.length(), "can not find " + patForClose.pattern());
            int end = matcher.end();
            text.append(string.subSequence(stringIdx, stringIdx + end));
            stringIdx += end;
            return;
        }
        if (string.charAt(stringIdx + 1) == '/') {
            if (stack.size() <= 1) {
                throw new TemplateParserException(string, stringIdx, "wrong close tag");
            }
            Matcher matcher = patForTagClose.matcher(string.subSequence(stringIdx, string.length()));
            if (!matcher.find()) throw new TemplateParserException(string, stringIdx, "no close tag found");
            int end = matcher.end();
            String tag = string.subSequence(stringIdx + 2, stringIdx + end - 1).toString().trim();
            if (stack.isEmpty() || stack.peek().getTag() == null) {
                throw new TemplateParserException(string, stringIdx, "wrong close tag");
            } else if(!tag.equals(stack.peek().getTag())) {
                throw new TemplateParserException(string, stack.peek().getLocation(), "tag " + stack.peek().getTag() + " not close");
            }
            flush();
            stack.pop();
            stringIdx += end;
            return;
        }
        int tagLocation = stringIdx;
        Matcher matcher = patForName.matcher(string.subSequence(stringIdx + 1, string.length()));
        if (!matcher.find()) throw new TemplateParserException(string, stringIdx, "no tag name");
        int end = matcher.end() + 1;
        String tag = string.subSequence(stringIdx + 1, stringIdx + end).toString().trim();
        increaseStringIndex(end);
        if (!String.valueOf(string.charAt(stringIdx)).matches("[\\s>/]")) {
            throw new TemplateParserException(string, stringIdx, "tag error");
        }
        flush();
        PlainElement parent = stack.peek();
        PlainElement element = new PlainElement(tag, tagLocation);
        parent.getContent().add(element);
        stack.push(element);
        parseAttributes();
    }

    private void parseAttributes() throws TemplateParserException {
        for (; ;) {
            Matcher matcher = patForNotBlank.matcher(string.subSequence(stringIdx, string.length()));
            if (!matcher.find()) throw new TemplateParserException(string, stack.peek().getLocation(), "tag " + stack.peek().getTag() + " not close");
            int start = matcher.start();
            stringIdx += start;
            char c = string.charAt(stringIdx);
            if (c == '>') {
                stringIdx++;
                String tag = stack.peek().getTag().toLowerCase();
                if (tag.equals("script")) {
                    matcher = patForScriptClose.matcher(string.subSequence(stringIdx, string.length()));
                    if (!matcher.find()) throw new TemplateParserException(string, stack.peek().getLocation(), "tag " + stack.peek().getTag() + " not close");
                    stack.pop().getContent().add(new PlainText(string.subSequence(stringIdx, stringIdx + matcher.start())));
                    stringIdx = stringIdx + matcher.end();
                }
                if (autoClosedTags.contains(tag)) {
                    stack.pop().nullContent(false);
                }
                return;
            }
            if (c == '/') {
                if (string.length() < stringIdx + 2 || string.charAt(stringIdx + 1) != '>') {
                    throw new TemplateParserException(string, stack.peek().getLocation(), "tag " + stack.peek().getTag() + " not close");
                }
                stack.pop().nullContent(true);
                stringIdx += 2;
                return;
            }
            matcher = patForName.matcher(string.subSequence(stringIdx, string.length()));
            if (!matcher.find()) throw new TemplateParserException(string, stack.peek().getLocation(), "tag " + stack.peek().getTag() + " not close");
            int end = matcher.end();
            String attr = string.subSequence(stringIdx, stringIdx + end).toString().trim();
            increaseStringIndex(end);
            matcher = patForEqual.matcher(string.subSequence(stringIdx, string.length()));
            if (!matcher.find()) {
                stack.peek().getAttributes().put(attr, null);
                continue;
            }
            end = matcher.end();
            increaseStringIndex(end);
            matcher = patForAttrValue.matcher(string.subSequence(stringIdx, string.length()));
            if (!matcher.find()) {
                stack.peek().getAttributes().put(attr, "");
                continue;
            }
            end = matcher.end();
            String value;
            if (string.charAt(stringIdx) == '"') {
                value = string.substring(stringIdx + 1, stringIdx + end - 1).replace("&quot;", "\"").replace("&#34;", "\"");
            } else if (string.charAt(stringIdx) == '\'') {
                value = string.substring(stringIdx + 1, stringIdx + end - 1).replace("&apos;", "'").replace("&#39;", "'");
            } else {
                value = string.substring(stringIdx, stringIdx + end);
            }
            increaseStringIndex(end);
            stack.peek().getAttributes().put(attr, value);
        }
    }

    private void increaseStringIndex(int count) throws TemplateParserException {
        stringIdx += count;
        if (stringIdx >= string.length()) {
            throw new TemplateParserException(string, stringIdx, "count overflow");
        }
    }

    private void flush() {
        if (text.length() > 0) {
            stack.peek().getContent().add(new PlainText(text));
            text.setLength(0);
        }
    }
}
