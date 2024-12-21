package org.Smart.ExpenseSplitter.exception;

public class PayeeNotFoundException extends RuntimeException {

    public PayeeNotFoundException(String message) {
        super(message);
    }

    public PayeeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
