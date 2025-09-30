package com.healthdata.user.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Password {
    private String value;

    private Password(String value) {
        this.value = value;
    }

    public static Password encode(String rawPassword, PasswordEncoder encoder) {
        validateStrength(rawPassword);
        String encoded = encoder.encode(rawPassword);
        return new Password(encoded);
    }

    public static Password fromEncoded(String encodedPassword) {
        return new Password(encodedPassword);
    }

    public boolean matches(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.value);
    }

    private static void validateStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }

        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
        if (!password.matches(passwordPattern)) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.");
        }
    }
}