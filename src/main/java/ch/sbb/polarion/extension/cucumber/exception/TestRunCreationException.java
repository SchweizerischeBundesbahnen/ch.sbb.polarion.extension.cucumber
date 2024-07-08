package ch.sbb.polarion.extension.cucumber.exception;

public class TestRunCreationException extends RuntimeException {

    private static final long serialVersionUID = 2654551444203246155L;

    public TestRunCreationException(String message) {
        super(message);
    }
}
