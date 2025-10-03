package com.healthdata.user.domain.model;

import com.healthdata.user.domain.vo.Email;
import com.healthdata.user.domain.vo.Password;
import com.healthdata.user.domain.vo.RecordKey;
import com.healthdata.user.domain.vo.UserId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    private UserId id;
    private String name;
    private String nickname;
    private Email email;
    private Password password;
    private RecordKey recordKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User(String name, String nickname, Email email, Password password, RecordKey recordKey) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.recordKey = recordKey;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static User create(String name, String nickname, String email, String rawPassword, PasswordEncoder encoder) {
        validateName(name);
        validateNickname(nickname);

        Email emailVo = Email.of(email);
        Password passwordVo = Password.encode(rawPassword, encoder);
        RecordKey recordKeyVo = RecordKey.generate();

        return new User(name, nickname, emailVo, passwordVo, recordKeyVo);
    }

    public static User fromPersistence(Long id, String name, String nickname, String email, String encodedPassword, String recordKey, LocalDateTime createdAt, LocalDateTime updatedAt) {
        User user = new User();
        user.id = UserId.of(id);
        user.name = name;
        user.nickname = nickname;
        user.email = Email.of(email);
        user.password = Password.fromEncoded(encodedPassword);
        user.recordKey = RecordKey.of(recordKey);
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        return user;
    }

    public boolean matchPassword(String rawPassword, PasswordEncoder encoder) {
        return this.password.matches(rawPassword, encoder);
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        if (name.length() < 2 || name.length() > 50) {
            throw new IllegalArgumentException("이름은 2자 이상 50자 이하여야 합니다.");
        }
    }

    private static void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
        if (nickname.length() < 2 || nickname.length() > 20) {
            throw new IllegalArgumentException("닉네임은 2자 이상 20자 이하여야 합니다.");
        }
        String nicknamePattern = "^[a-zA-Z0-9가-힣]+$";
        if (!nickname.matches(nicknamePattern)) {
            throw new IllegalArgumentException("닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.");
        }
    }
}