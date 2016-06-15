package cn.webwheel.setters;

public class ShortArraySetter extends AbstractSetter<short[]> {

    @Override
    protected short[] get(Object param) {
        if (param instanceof String[]) {
            String[] ss = (String[]) param;
            short[] ds = new short[ss.length];
            for (int i = 0; i < ds.length; i++) {
                try {
                    ds[i] = Short.parseShort(ss[i]);
                } catch (NumberFormatException ignored) {
                }
            }
            return ds;
        }
        return null;
    }
}
