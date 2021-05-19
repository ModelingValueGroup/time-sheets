package nl.modelingvalue.timesheets.util;

public class FatalException extends RuntimeException {
    public FatalException(Exception e) {
        super(e);
    }

    public FatalException(String msg) {
        super(msg);
    }

    public FatalException(String msg, Exception e) {
        super(msg, e);
    }
}
