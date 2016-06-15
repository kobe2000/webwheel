package cn.webwheel.setters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateSetter extends AbstractSetter<Date> {

    final static SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
    final static SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static Date parse(String str) throws ParseException {
        if (str == null) return null;
        SimpleDateFormat sdf = df1;
        if (str.length() > 10) sdf = df2;
        synchronized (sdf) {
            return sdf.parse(str);
        }
    }

    @Override
    protected Date get(Object param) {
        if (param instanceof String[]) {
            try {
                return parse(((String[]) param)[0]);
            } catch (ParseException e) {
                return null;
            }
        }
        return null;
    }
}
