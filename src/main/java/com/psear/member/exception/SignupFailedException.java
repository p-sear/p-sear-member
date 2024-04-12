package com.psear.member.exception;

public class SignupFailedException extends RuntimeException {
    public SignupFailedException() {
        this("가입할 수 없는 유저입니다");
    }

    public SignupFailedException(String message) {
        super(message);
    }
}
