package cn.webwheel.setters;

public class ByteSetter extends AbstractSetter<Byte> {

    public ByteSetter(Byte def) {
        this.def = def;
    }

    @Override
    protected Byte get(Object param) {
        if (param instanceof String[]) {
            try {
                return Byte.valueOf(((String[]) param)[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
