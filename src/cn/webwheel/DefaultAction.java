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

import cn.webwheel.results.JsonResult;
import cn.webwheel.results.RedirectResult;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Default action implement.
 */
abstract public class DefaultAction implements WebContextAware {

    public static Pattern PAT_EMAIL = Pattern.compile("[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*");
    public static Pattern PAT_ID = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");

    public static String defPagePkg;

    protected WebContext ctx;

    @Override
    public void setWebContext(WebContext ctx) {
        this.ctx = ctx;
    }

    public String getPagePkg() {
        return defPagePkg;
    }

    public String getPathCharset() {
        return "utf-8";
    }

    public void addCookie(String k, String v) {
        k = urlEncode(k);
        v = urlEncode(v);
        Cookie cookie;
        if (v == null) {
            cookie = new Cookie(k, "");
            cookie.setMaxAge(0);
        } else {
            cookie = new Cookie(k, v);
        }
        ctx.getResponse().addCookie(cookie);
    }

    public void addCookie(String k, String v, int maxAge, String domain) {
        k = urlEncode(k);
        v = urlEncode(v);
        Cookie cookie = new Cookie(k, v);
        cookie.setMaxAge(maxAge);
        if (domain != null) {
            cookie.setDomain(domain);
        }
        ctx.getResponse().addCookie(cookie);
    }

    public String getCookie(String k) {
        Cookie[] cookies = ctx.getRequest().getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (urlDecode(cookie.getName()).equals(k)) {
                return urlDecode(cookie.getValue());
            }
        }
        return null;
    }

    public void setHeader(String k, String v) {
        ctx.getResponse().setHeader(k, v);
    }

    public void setDateHeader(String k, long v) {
        ctx.getResponse().setDateHeader(k, v);
    }

    public void setIntHeader(String k, int v) {
        ctx.getResponse().setIntHeader(k, v);
    }

    public String getHeader(String k) {
        return ctx.getRequest().getHeader(k);
    }

    public int getIntHeader(String k) {
        return ctx.getRequest().getIntHeader(k);
    }

    public long getDateHeader(String k) {
        return ctx.getRequest().getDateHeader(k);
    }

    public void setSessionAttr(String k, Object v) {
        if (v == null) {
            HttpSession session = ctx.getRequest().getSession(false);
            if (session == null) return;
            session.removeAttribute(k);
        } else {
            HttpSession session = ctx.getRequest().getSession();
            session.setAttribute(k, v);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getSessionAttr(String k) {
        HttpSession session = ctx.getRequest().getSession(false);
        if (session == null) return null;
        return (T) session.getAttribute(k);
    }

    public void setRequestAttr(String k, Object v) {
        ctx.getRequest().setAttribute(k, v);
    }

    public <T> T getRequestAttr(String k) {
        return (T) ctx.getRequest().getAttribute(k);
    }

    public void setApplicationAttr(String k, Object v) {
        ctx.getContext().setAttribute(k, v);
    }

    public <T> T getApplicationAttr(String k) {
        return (T) ctx.getContext().getAttribute(k);
    }

    public void invalidateSession() {
        HttpSession session = ctx.getRequest().getSession(false);
        if (session == null) return;
        session.invalidate();
    }

    /**
     * get a json result with "ok" property.
     */
    public JsonResult ok() {
        return new JsonResult().set("ok", 1);
    }

    /**
     * get a json result with "msg" property.
     */
    public JsonResult err(String msg) {
        return new JsonResult().set("msg", msg);
    }

    public <T> T notNull(T obj, String msg) {
        if (obj == null) {
            validFailed(msg);
        }
        return obj;
    }

    public <T> T notNull(T obj) {
        return notNull(obj, null);
    }

    public <T extends Comparable<T>> T min(T val, T min, String msg) {
        if (val == null) return null;
        if (val.compareTo(min) > 0) {
            validFailed(msg);
        }
        return val;
    }

    public <T extends Comparable<T>> T min(T val, T min) {
        return min(val, min, null);
    }

    public <T extends Comparable<T>> T max(T val, T max, String msg) {
        if (val == null) return null;
        if (val.compareTo(max) < 0) {
            validFailed(msg);
        }
        return val;
    }

    public <T extends Comparable<T>> T max(T val, T max) {
        return min(val, max, null);
    }

    public <T extends Comparable<T>> T range(T val, T min, T max, String msg) {
        return max(min(val, min, msg), max, msg);
    }

    public <T extends Comparable<T>> T range(T val, T min, T max) {
        return range(val, min, max, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Number> T min(T val, T min, String msg) {
        if (val == null) return null;
        if (val instanceof Byte || val instanceof Short || val instanceof Integer || val instanceof Long) {
            if (val.longValue() < min.longValue()) {
                validFailed(msg);
            }
            return val;
        }
        if (val instanceof Float || val instanceof Double) {
            if (val.doubleValue() < min.doubleValue()) {
                validFailed(msg);
            }
            return val;
        }
        if (val instanceof BigDecimal || val instanceof BigInteger) {
            if (((Comparable) val).compareTo(min) < 0) {
                validFailed(msg);
            }
            return val;
        }
        BigDecimal bv = new BigDecimal(val.toString());
        if (bv.compareTo(new BigDecimal(min.toString())) < 0) {
            validFailed(msg);
        }
        return val;
    }

    public <T extends Number> T min(T val, T min) {
        return min(val, min, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Number> T max(T val, T max, String msg) {
        if (val == null) return null;
        if (val instanceof Byte || val instanceof Short || val instanceof Integer || val instanceof Long) {
            if (val.longValue() > max.longValue()) {
                validFailed(msg);
            }
            return val;
        }
        if (val instanceof Float || val instanceof Double) {
            if (val.doubleValue() > max.doubleValue()) {
                validFailed(msg);
            }
            return val;
        }
        if (val instanceof BigDecimal || val instanceof BigInteger) {
            if (((Comparable) val).compareTo(max) > 0) {
                validFailed(msg);
            }
            return val;
        }
        BigDecimal bv = new BigDecimal(val.toString());
        if (bv.compareTo(new BigDecimal(max.toString())) > 0) {
            validFailed(msg);
        }
        return val;
    }

    public <T extends Number> T max(T val, T max) {
        return min(val, max, null);
    }

    public <T extends Number> T range(T val, T min, T max, String msg) {
        return max(min(val, min, msg), max, msg);
    }

    public <T extends Number> T range(T val, T min, T max) {
        return range(val, min, max, null);
    }

    public String min(String val, int min, String msg) {
        if (val == null) return null;
        if (val.length() < min) {
            validFailed(msg);
        }
        return val;
    }

    public String min(String val, int min) {
        return min(val, min, null);
    }

    public String notEmpty(String val, String msg) {
        return min(val, 1, msg);
    }

    public String notEmpty(String val) {
        return notEmpty(val, null);
    }

    public String max(String val, int max, String msg) {
        if (val == null) return null;
        if (val.length() > max) {
            validFailed(msg);
        }
        return val;
    }

    public String max(String val, int max) {
        return min(val, max, null);
    }

    public String range(String val, int min, int max, String msg) {
        return max(min(val, min, msg), max, msg);
    }

    public String range(String val, int min, int max) {
        return range(val, min, max, null);
    }

    public String length(String val, int len, String msg) {
        return range(val, len, len, msg);
    }

    public String length(String val, int len) {
        return length(val, len, null);
    }

    public <T> T equal(T val, T dst, String msg) {
        if (val == null) return null;
        if (!val.equals(dst)) {
            validFailed(msg);
        }
        return val;
    }

    public <T> T equal(T val, T dst) {
        return equal(val, dst, null);
    }

    public <T> T unequal(T val, T dst, String msg) {
        if (val == null) return null;
        if (val.equals(dst)) {
            validFailed(msg);
        }
        return val;
    }

    public <T> T unequal(T val, T dst) {
        return unequal(val, dst, null);
    }

    public String trim(String val) {
        if (val == null) return null;
        return val.trim();
    }

    public String nullIfEmpty(String val) {
        if (val == null) return null;
        if (val.isEmpty()) return null;
        return val;
    }

    public String emptyIfNull(String val) {
        if (val == null) return "";
        return val;
    }

    public String replace(String val, String target, String replacement) {
        if (val == null) return null;
        return val.replace(target, replacement);
    }

    public String replaceAll(String val, String pattern, String replacement) {
        if (val == null) return null;
        return val.replaceAll(pattern, replacement);
    }

    public String replaceAll(String val, Pattern pattern, String replacement) {
        if (val == null) return null;
        return pattern.matcher(val).replaceAll(replacement);
    }

    public String match(String val, String pattern, String msg) {
        if (val == null) return null;
        if (!val.matches(pattern)) {
            validFailed(msg);
        }
        return val;
    }

    public String match(String val, String pattern) {
        return match(val, pattern, null);
    }

    public String match(String val, Pattern pattern, String msg) {
        if (val == null) return null;
        if (!pattern.matcher(val).matches()) {
            validFailed(msg);
        }
        return val;
    }

    public String match(String val, Pattern pattern) {
        return match(val, pattern, null);
    }

    public String email(String val, String msg) {
        return match(val, PAT_EMAIL, msg);
    }

    public String email(String val) {
        return email(val, null);
    }

    public Date date(String val, String pattern, String msg) {
        if (val == null) return null;
        try {
            return new SimpleDateFormat(pattern).parse(val);
        } catch (ParseException e) {
            validFailed(msg);
            return null;
        }
    }

    public Date date(String val, String pattern) {
        return date(val, pattern, null);
    }

    public Number number(String val, String pattern, String msg) {
        if (val == null) return null;
        try {
            return new DecimalFormat(pattern).parse(pattern);
        } catch (ParseException e) {
            validFailed(msg);
            return null;
        }
    }

    public Number number(String val, String pattern) {
        return number(val, pattern, null);
    }

    public String id(String val, String msg) {
        return match(val, PAT_ID, msg);
    }

    public String id(String val) {
        return id(val, null);
    }

    public void affirm(boolean val, String msg) {
        if (!val) {
            validFailed(msg);
        }
    }

    public void affirm(boolean val) {
        affirm(val, null);
    }

    public void validFailed(String msg) {
        throw new LogicException(err(msg == null ? "validation failed" : msg));
    }

    public RedirectResult redirect(Class<? extends DefaultPage> cls) {
        return redirect(cls, null);
    }

    public RedirectResult redirect(Class<? extends DefaultPage> cls, String anchor, Object... kvs) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < kvs.length - 1; i += 2) {
            map.put(kvs[i].toString(), kvs[i + 1]);
        }
        return redirect(cls, anchor, map);
    }

    public RedirectResult redirect(Class<? extends DefaultPage> cls, String anchor, Map<String, Object> params) {
        String path = path(cls);
        String qs = paramsToStr(params);
        if (qs != null) path += '?' + qs;
        if (anchor != null) path += '#' + anchor;
        return new RedirectResult(path);
    }

    public String path(Class<? extends DefaultPage> cls) {
        String pkg = getPagePkg();
        if (pkg == null) throw new IllegalStateException("pagePkg config wrong");
        String cname = cls.getName();
        if (cname.length() <= pkg.length()) return null;
        return "/" + cname.substring(pkg.length() + 1).replace('.', '/') + ".html";
    }

    @SuppressWarnings("deprecation")
    public String urlEncode(String s) {
        if (s == null) return null;
        String charset = getPathCharset();
        if (charset == null) {
            return URLEncoder.encode(s);
        } else {
            try {
                return URLEncoder.encode(s, charset);
            } catch (UnsupportedEncodingException e) {
                return URLEncoder.encode(s);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public String urlDecode(String s) {
        if (s == null) return null;
        String charset = getPathCharset();
        if (charset == null) {
            return URLDecoder.decode(s);
        } else {
            try {
                return URLDecoder.decode(s, charset);
            } catch (UnsupportedEncodingException e) {
                return URLDecoder.decode(s);
            }
        }
    }

    public String paramsToStr(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String k = urlEncode(entry.getKey());
            Object val = entry.getValue();
            if (val.getClass().isArray()) {
                int len = Array.getLength(val);
                for (int i = 0; i < len; i++) {
                    sb.append(k).append('=').append(urlEncode(String.valueOf(Array.get(val, i)))).append('&');
                }
            } else {
                sb.append(k).append('=').append(urlEncode(String.valueOf(entry.getValue()))).append('&');
            }
        }
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
