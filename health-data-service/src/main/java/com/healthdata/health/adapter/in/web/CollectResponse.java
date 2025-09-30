package com.healthdata.health.adapter.in.web;

public record CollectResponse(
    String message,
    int collectedCount
) {}