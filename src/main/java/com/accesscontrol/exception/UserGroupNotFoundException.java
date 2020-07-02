package com.accesscontrol.exception;

public class UserGroupNotFoundException extends RuntimeException {
    public UserGroupNotFoundException() {
    }

    public UserGroupNotFoundException(String message) {
        super(message);
    }

    public UserGroupNotFoundException(Throwable cause) {
        super(cause);
    }
}
