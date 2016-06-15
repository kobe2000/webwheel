package cn.webwheel.setters;

public class ShortSetter extends AbstractSetter<Short> {

    public ShortSetter(Short def) {
        this.def = def;
    }

    @Override
    protected Short get(Object param) {
        if (param instanceof String[]) {
            try {
                return Short.valueOf(((String[]) param)[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
