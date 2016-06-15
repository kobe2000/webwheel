package cn.webwheel.setters;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class ValueOfArraySetter extends AbstractSetter {

    private Method method;
    private Class cls;

    public ValueOfArraySetter(Method method) {
        this.method = method;
        cls = method.getReturnType();
    }

    @Override
    protected Object get(Object param) {
        if (param instanceof String[]) {
            String[] ss = (String[]) param;
            Object arr = Array.newInstance(cls, ss.length);
            for (int i = 0; i < ss.length; i++) {
                try {
                    Array.set(arr, i, method.invoke(null, ss[i]));
                } catch (Exception ignored) {
                }
            }
            return arr;
        }
        return null;
    }

    public static ValueOfArraySetter create(Class cls) {
        if (!cls.isArray()) return null;
        cls = cls.getComponentType();
        Method method = ValueOfSetter.getValueOfMethod(cls);
        if (method == null) return null;
        return new ValueOfArraySetter(method);
    }
}
