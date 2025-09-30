package com.healthdata.health.application.service;

public class DataDuplicationException extends RuntimeException {
    public DataDuplicationException(String message) {
        super(message);
    }
}