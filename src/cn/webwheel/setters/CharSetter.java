package cn.webwheel.setters;

public class CharSetter extends AbstractSetter<Character> {

    public CharSetter(Character def) {
        this.def = def;
    }

    @Override
    protected Character get(Object param) {
        if (param instanceof String[]) {
            String s = ((String[]) param)[0];
            if (s.length() == 1) {
                return s.charAt(0);
            }
        }
        return null;
    }
}
