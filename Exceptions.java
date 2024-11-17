import java.lang.Exception;

class OccurCheckException extends Exception {
    public OccurCheckException(String message) {
        super(message);
    }
}

class UnificationException extends Exception {
    public UnificationException(String message) {
        super(message);
    }
}

class TimeoutException extends Exception {
    public static final long TIMEOUT = 10; // Timeout en millisecondes

    public TimeoutException() {
        super("Timeout : " + TIMEOUT + "ms");
    }
}
