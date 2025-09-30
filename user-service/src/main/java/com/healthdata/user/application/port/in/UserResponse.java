package com.healthdata.user.application.port.in;

import com.healthdata.user.domain.model.User;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String name,
    String nickname,
    String email,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId() != null ? user.getId().getValue() : null,
                user.getName(),
                user.getNickname(),
                user.getEmail().getValue(),
                user.getCreatedAt()
        );
    }
}