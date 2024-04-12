package com.psear.member.exception;

public class MailServerException extends RuntimeException {
    public MailServerException() {
        this("메일서버의 오류");
    }

    public MailServerException(String message) {
        super(message);
    }
}
