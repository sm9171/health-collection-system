package com.healthdata.user.application.port.in;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterUserCommand {
    String name;
    String nickname;
    String email;
    String password;
}