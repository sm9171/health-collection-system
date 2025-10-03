package com.healthdata.user.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthdata.user.application.port.in.LoginUseCase;
import com.healthdata.user.application.port.in.RegisterUserUseCase;
import com.healthdata.user.application.port.in.UserResponse;
import com.healthdata.user.application.service.DuplicateEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("User API 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;
    
    @MockitoBean
    private LoginUseCase loginUseCase;
    
    @Test
    @DisplayName("회원가입 API 정상 동작")
    @WithMockUser
    void register() throws Exception {
        // given
        RegisterRequest request = createRegisterRequest();
        UserResponse userResponse = new UserResponse(
                1L,
                "홍길동",
                "gildong",
                "hong@example.com",
                LocalDateTime.now()
        );
                
        when(registerUserUseCase.register(any())).thenReturn(userResponse);
        
        // when & then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.email").value("hong@example.com"))
            .andExpect(jsonPath("$.name").value("홍길동"));
    }
    
    @Test
    @DisplayName("중복 이메일로 회원가입 시 409 응답")
    @WithMockUser
    void registerWithDuplicateEmail() throws Exception {
        // given
        RegisterRequest request = createRegisterRequest();
        when(registerUserUseCase.register(any()))
                .thenThrow(new DuplicateEmailException("이미 사용 중인 이메일입니다."));
        
        // when & then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("USER_001"))
            .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }
    
    @Test
    @DisplayName("잘못된 입력값으로 회원가입 시 400 응답")
    @WithMockUser
    void registerWithInvalidInput() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest(null, null, null, null); // empty request
        
        // when & then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }
    
    private RegisterRequest createRegisterRequest() {
        return new RegisterRequest(
                "홍길동",
                "gildong", 
                "hong@example.com",
                "Password123!"
        );
    }
}