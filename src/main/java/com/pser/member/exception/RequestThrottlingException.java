package com.pser.member.exception;

public class RequestThrottlingException extends RuntimeException {
    public RequestThrottlingException() {
        this("요청 한계를 초과하였습니다.");
    }

    public RequestThrottlingException(String message) {
        super(message);
    }
}
