#Action Setter
When action is invoked, http parameters can be inject to action through WebWheel framework.
With `DefautMain` the following types are supported.
```java
public class MyAction {
    @cn.webwheel.WebParam("p1")
    public String p1;
    public String[] p2;
    public boolean p3;
    public Boolean p4;
    public boolean[] p5;
    public byte p6;
    public Byte p7;
    public byte[] p8;
    public short p9;
    public Short p10;
    public short[] p11;
    public int p12;
    public Integer p13;
    public int[] p14;
    public long p15;
    public Long p16;
    public long[] p17;
    public float p18;
    public Float p19;
    public float[] p20;
    public double p21;
    public Double p22;
    public double[] p23;
    public File p24;
    public File[] p25;
    public FileEx p26;
    public FileEx[] p27;
    public Map<String, Object> p28;
    public Map<String, String> p29;
    public Map<String, String[]> p30;
    public Map<String, File> p31;
    public Map<String, File[]> p32;
    public Map<String, FileEx> p33;
    public Map<String, FileEx[]> p34;
}
```
The parameter's name is the field name by default(by array type, a `[]` will be added to parameter's name).
It can be changed by `WebParam` annotation.

Public fields or setter or method parameters can be injected. This depends on action method's `Action#setterPolicy`.
```java
public class MyAction {
    public String f;
    private String s;
    public void setS(String s) {
        this.s = s;
    }
    @cn.webwheel.Action(setterPolicy = cn.webwheel.SetterPolicy.Auto)
    public void execute(String p) {
    }
}
```
##Extends
You can add more action setters in WebWheel. Here is a `Date` type action setter.
```java
public class MyMain extends cn.webwheel.DefaultMain {
    public void init() {
        super.init();
        set(Date.class).by(new cn.webwheel.setters.AbstractSetter<java.util.Date>() {
             protected T get(Object param) {
                if(param instanceof String[]) {
                    new java.text.SimpleDateFormat("yyyyMMdd").parse(((String[])param)[0]);
                }
                return null;
            }
        });
    }
}
```
##Json Property
If a parameter injection site marked with `JsonProperty`,
WebWheel will call `Jackson` framework to do a string to object conversion.
```java
public class MyAction {
    @com.fasterxml.jackson.annotation.JsonProperty
    public SomeBean bean;
}
```