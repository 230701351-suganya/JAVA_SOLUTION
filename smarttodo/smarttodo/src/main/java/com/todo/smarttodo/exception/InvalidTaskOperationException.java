package com.todo.smarttodo.exception;

public class InvalidTaskOperationException extends RuntimeException {
    public InvalidTaskOperationException(String message) {
        super(message);
    }
}
