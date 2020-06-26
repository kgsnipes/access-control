package com.accesscontrol.beans;

public class AccessControlContext {

    private String userId;

    private String message;

    public AccessControlContext() {
    }

    public AccessControlContext(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
