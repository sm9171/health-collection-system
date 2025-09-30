package com.healthdata.user.application.port.in;

public interface RegisterUserUseCase {
    UserResponse register(RegisterUserCommand command);
}
