package cn.webwheel.setters;

import cn.webwheel.ActionSetter;
import cn.webwheel.FileEx;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

public class WebParams {

    private Map<String, Object> params;
    private ActionSetter actionSetter;

    WebParams(Map<String, Object> params, ActionSetter actionSetter) {
        this.params = params;
        this.actionSetter = actionSetter;
    }

    public <T> T getBean(Class<T> type, String name) {
        return (T) BeanSetter.create(type, actionSetter).set(null, null, params, name);
    }

    public boolean[] getBooleanArray(String name) {
        return new BooleanArraySetter().set(null, null, params, name);
    }

    public boolean getBoolean(String name) {
        return new BooleanSetter(false).set(null, null, params, name);
    }

    public byte[] getByteArray(String name) {
        return new ByteArraySetter().set(null, null, params, name);
    }

    public Byte getByte(String name) {
        return new ByteSetter(null).set(null, null, params, name);
    }

    public char getChar(String name) {
        return new CharSetter(null).set(null, null, params, name);
    }

    public char[] getCharArray(String name) {
        return new CharArraySetter().set(null, null, params, name);
    }

    public Date getDate(String name) {
        return new DateSetter().set(null, null, params, name);
    }

    public Date[] getDateArray(String name) {
        return new DateArraySetter().set(null, null, params, name);
    }

    public double[] getDoubleArray(String name) {
        return new DoubleArraySetter().set(null, null, params, name);
    }

    public Double getDouble(String name) {
        return new DoubleSetter(null).set(null, null, params, name);
    }

    public File[] getFileArray(String name) {
        return new FileArraySetter().set(null, null, params, name);
    }

    public FileEx[] getFileExArray(String name) {
        return new FileExArraySetter().set(null, null, params, name);
    }

    public FileEx getFileEx(String name) {
        return new FileExSetter().set(null, null, params, name);
    }

    public File getFile(String name) {
        return new FileSetter().set(null, null, params, name);
    }

    public float[] getFloatArray(String name) {
        return new FloatArraySetter().set(null, null, params, name);
    }

    public Float getFloat(String name) {
        return new FloatSetter(null).set(null, null, params, name);
    }

    public int[] getIntArray(String name) {
        return new IntArraySetter().set(null, null, params, name);
    }

    public Integer getInt(String name) {
        return new IntSetter(null).set(null, null, params, name);
    }

    public <T> T getJson(Type type, String name) {
        return (T) new JSonSetter(type).set(null, null, params, name);
    }

    public <T> T getValueOf(Class cls, String name) {
        Method method = ValueOfSetter.getValueOfMethod(cls);
        if (method == null) return null;
        return (T) new ValueOfSetter(method).set(null, null, params, name);
    }

    public <T> T getValueOfArray(Class cls, String name) {
        Method method = ValueOfSetter.getValueOfMethod(cls);
        if (method == null) return null;
        return (T) new ValueOfArraySetter(method).set(null, null, params, name);
    }

    public long[] getLongArray(String name) {
        return new LongArraySetter().set(null, null, params, name);
    }

    public Long getLong(String name) {
        return new LongSetter(null).set(null, null, params, name);
    }

    public short[] getShortArray(String name) {
        return new ShortArraySetter().set(null, null, params, name);
    }

    public Short getShort(String name) {
        return new ShortSetter(null).set(null, null, params, name);
    }

    public String getString(String name) {
        return new StringSetter().set(null, null, params, name);
    }

    public String[] getStringArray(String name) {
        return new StringArraySetter().set(null, null, params, name);
    }
}
