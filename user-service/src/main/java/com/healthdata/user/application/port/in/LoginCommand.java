package com.healthdata.user.application.port.in;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginCommand {
    String email;
    String password;
}