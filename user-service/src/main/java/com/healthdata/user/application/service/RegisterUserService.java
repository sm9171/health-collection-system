package com.healthdata.user.application.service;

import com.healthdata.user.application.port.in.RegisterUserCommand;
import com.healthdata.user.application.port.in.RegisterUserUseCase;
import com.healthdata.user.application.port.in.UserResponse;
import com.healthdata.user.application.port.out.UserRepository;
import com.healthdata.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }

        User user = User.create(
                command.getName(),
                command.getNickname(),
                command.getEmail(),
                command.getPassword(),
                passwordEncoder
        );

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }
}