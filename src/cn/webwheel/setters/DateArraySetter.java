package cn.webwheel.setters;

import java.text.ParseException;
import java.util.Date;

public class DateArraySetter extends AbstractSetter<Date[]> {
    @Override
    protected Date[] get(Object param) {
        if (param instanceof String[]) {
            String[] ss = (String[]) param;
            Date[] ds = new Date[ss.length];
            for (int i = 0; i < ds.length; i++) {
                try {
                    ds[i] = DateSetter.parse(ss[i]);
                } catch (ParseException ignored) {
                }
            }
            return ds;
        }
        return null;
    }
}
