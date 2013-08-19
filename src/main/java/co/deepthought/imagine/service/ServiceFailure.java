package co.deepthought.imagine.service;

/**
 * JSON-serializable class to indicate a failure.
 */
public class ServiceFailure extends Exception {

    private final String message;

    public ServiceFailure(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}