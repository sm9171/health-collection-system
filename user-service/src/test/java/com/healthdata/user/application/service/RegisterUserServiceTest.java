package com.healthdata.user.application.service;

import com.healthdata.user.application.port.in.RegisterUserCommand;
import com.healthdata.user.application.port.in.UserResponse;
import com.healthdata.user.application.port.out.UserRepository;
import com.healthdata.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("회원가입 Use Case 테스트")
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private RegisterUserService registerUserService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    @DisplayName("정상적으로 회원가입을 처리할 수 있다")
    void register() {
        // given
        RegisterUserCommand command = RegisterUserCommand.builder()
            .name("홍길동")
            .nickname("gildong")
            .email("hong@example.com")
            .password("Password123!")
            .build();
            
        when(userRepository.existsByEmail(command.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(command.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return User.fromPersistence(1L, user.getName(), user.getNickname(), 
                    user.getEmail().getValue(), user.getPassword().getValue(),
                    user.getCreatedAt(), user.getUpdatedAt());
        });
        
        // when
        UserResponse response = registerUserService.register(command);
        
        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(command.getEmail());
        assertThat(response.name()).isEqualTo(command.getName());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("중복된 이메일로 회원가입을 시도하면 예외가 발생한다")
    void registerWithDuplicateEmail() {
        // given
        RegisterUserCommand command = RegisterUserCommand.builder()
            .name("홍길동")
            .nickname("gildong")
            .email("hong@example.com")
            .password("Password123!")
            .build();
            
        when(userRepository.existsByEmail(command.getEmail())).thenReturn(true);
        
        // when & then
        assertThatThrownBy(() -> registerUserService.register(command))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessage("이미 사용 중인 이메일입니다.");
        
        verify(userRepository, never()).save(any(User.class));
    }
}