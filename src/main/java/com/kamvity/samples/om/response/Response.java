package com.kamvity.samples.om.response;

public abstract class Response {

    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSensitiveMessage() {
        return sensitiveMessage;
    }

    public void setSensitiveMessage(String sensitiveMessage) {
        this.sensitiveMessage = sensitiveMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String status;

    public String errorMessage;

    public String sensitiveMessage;

}
