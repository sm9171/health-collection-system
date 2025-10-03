package com.healthdata.user.application.service;

import com.healthdata.user.application.port.in.LoginCommand;
import com.healthdata.user.application.port.in.LoginResponse;
import com.healthdata.user.application.port.in.LoginUseCase;
import com.healthdata.user.application.port.in.UserResponse;
import com.healthdata.user.application.port.out.UserRepository;
import com.healthdata.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginService implements LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginCommand command) {
        User user = userRepository.findByEmail(command.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!user.matchPassword(command.getPassword(), passwordEncoder)) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateToken(user.getEmail().getValue(), user.getRecordKey().getValue());

        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtTokenProvider.getExpirationTime(),
                UserResponse.from(user)
        );
    }
}