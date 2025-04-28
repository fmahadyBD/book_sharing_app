package com.fmahadybd.book_network_api_service.exception;

public class OperationNotPermittedException extends RuntimeException {
    public OperationNotPermittedException() {
    }

    public OperationNotPermittedException(String message) {
        super(message);
    }

}
