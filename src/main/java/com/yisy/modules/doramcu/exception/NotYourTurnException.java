package com.yisy.modules.doramcu.exception;

public class NotYourTurnException extends IllegalStateException {
    public NotYourTurnException(String message) {
        super(message);
    }
}
