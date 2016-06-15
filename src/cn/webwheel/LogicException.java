package cn.webwheel;

public class LogicException extends RuntimeException {

    private static final long serialVersionUID = 3870244889834857271L;

    private Object result;

    public LogicException(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }
}
