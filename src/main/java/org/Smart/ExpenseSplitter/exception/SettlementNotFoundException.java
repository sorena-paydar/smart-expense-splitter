package org.Smart.ExpenseSplitter.exception;

public class SettlementNotFoundException extends RuntimeException {

    public SettlementNotFoundException(String message) {
        super(message);
    }

    public SettlementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
