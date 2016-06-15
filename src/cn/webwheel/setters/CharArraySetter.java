package cn.webwheel.setters;

public class CharArraySetter extends AbstractSetter<char[]> {
    @Override
    protected char[] get(Object param) {
        if (param instanceof String[]) {
            String[] ss = (String[]) param;
            char[] ds = new char[ss.length];
            for (int i = 0; i < ds.length; i++) {
                if (ss[i].length() == 1) {
                    ds[i] = ss[i].charAt(0);
                }
            }
            return ds;
        }
        return null;
    }
}
