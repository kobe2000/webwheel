package cn.webwheel.setters;

import cn.webwheel.ActionSetter;

import java.lang.reflect.Member;
import java.util.Map;

public class WebParamsSetter extends AbstractSetter<WebParams> {

    ActionSetter actionSetter;

    public WebParamsSetter(ActionSetter actionSetter) {
        this.actionSetter = actionSetter;
    }

    @Override
    public WebParams set(Object instance, Member member, Map<String, Object> params, String paramName) {
        WebParams wp = new WebParams(params, actionSetter);
        set(instance, member, wp);
        return wp;
    }
}
