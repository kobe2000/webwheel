package cn.webwheel;

import cn.webwheel.results.RedirectResult;
import cn.webwheel.results.TemplateResult;

import java.util.Map;

abstract public class DefaultPage extends DefaultAction {

    @Action
    public Object html() throws Exception {
        return new TemplateResult(this, path());
    }

    public RedirectResult redirect() {
        return super.redirect(getClass());
    }

    public RedirectResult redirect(String anchor, Object... kvs) {
        return super.redirect(getClass(), anchor, kvs);
    }

    public RedirectResult redirect(String anchor, Map<String, Object> params) {
        return super.redirect(getClass(), anchor, params);
    }

    public String path() {
        return path(getClass());
    }
}
