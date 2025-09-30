package com.healthdata.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User 도메인 테스트")
class UserTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("유효한 정보로 사용자를 생성할 수 있다")
    void createUser() {
        // given
        String name = "홍길동";
        String nickname = "gildong";
        String email = "hong@example.com";
        String password = "Password123!";
        
        // when
        User user = User.create(name, nickname, email, password, passwordEncoder);
        
        // then
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getEmail().getValue()).isEqualTo(email);
        assertThat(user.matchPassword(password, passwordEncoder)).isTrue();
    }
    
    @Test
    @DisplayName("잘못된 이메일 형식으로 사용자를 생성할 수 없다")
    void createUserWithInvalidEmail() {
        // given
        String invalidEmail = "invalid-email";
        
        // when & then
        assertThatThrownBy(() -> 
            User.create("홍길동", "gildong", invalidEmail, "Password123!", passwordEncoder)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("Invalid email format");
    }

    @Test
    @DisplayName("약한 비밀번호로 사용자를 생성할 수 없다")
    void createUserWithWeakPassword() {
        // given
        String weakPassword = "weak";
        
        // when & then
        assertThatThrownBy(() -> 
            User.create("홍길동", "gildong", "hong@example.com", weakPassword, passwordEncoder)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("비밀번호는 8자 이상이어야 합니다.");
    }

    @Test
    @DisplayName("비밀번호 매칭을 확인할 수 있다")
    void matchPassword() {
        // given
        String password = "Password123!";
        User user = User.create("홍길동", "gildong", "hong@example.com", password, passwordEncoder);
        
        // when & then
        assertThat(user.matchPassword(password, passwordEncoder)).isTrue();
        assertThat(user.matchPassword("wrongpassword", passwordEncoder)).isFalse();
    }
}