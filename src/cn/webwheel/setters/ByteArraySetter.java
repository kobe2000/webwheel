package cn.webwheel.setters;

public class ByteArraySetter extends AbstractSetter<byte[]> {
    @Override
    protected byte[] get(Object param) {
        if (param instanceof String[]) {
            String[] ss = (String[]) param;
            byte[] ds = new byte[ss.length];
            for (int i = 0; i < ds.length; i++) {
                try {
                    ds[i] = Byte.parseByte(ss[i]);
                } catch (NumberFormatException ignored) {
                }
            }
            return ds;
        }
        return null;
    }
}
