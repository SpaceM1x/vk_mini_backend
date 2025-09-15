package com.hackaton.vk_mini_backend.controller;

import com.hackaton.vk_mini_backend.dto.request.*;
import com.hackaton.vk_mini_backend.dto.response.JwtResponse;
import com.hackaton.vk_mini_backend.dto.response.MessageResponse;
import com.hackaton.vk_mini_backend.dto.response.TokenRefreshResponse;
import com.hackaton.vk_mini_backend.service.AuthService;
import com.hackaton.vk_mini_backend.service.VkAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Методы для аутентификации пользователей")
public class AuthController {

    private final AuthService authService;

    private final VkAuthService vkAuthService;

    @Value("${vk.auth.enableServiceLogin:false}")
    private boolean enableServiceLogin;

    @PostMapping("/login")
    @Operation(
            summary = "Аутентификация пользователя",
            description = "Позволяет пользователю войти в систему и получить JWT токен")
    public ResponseEntity<JwtResponse> authenticateUser(
            @Parameter(description = "Данные для входа", required = true) @Valid @RequestBody
                    final LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создаёт нового пользователя в системе")
    public ResponseEntity<MessageResponse> registerUser(
            @Parameter(description = "Данные для регистрации", required = true) @Valid @RequestBody
                    final RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/refreshtoken")
    @Operation(
            summary = "Обновление JWT токена",
            description = "Позволяет получить новый токен на основе refresh token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @Parameter(description = "Refresh токен", required = true) @Valid @RequestBody
                    final RefreshTokenRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/vk-service-user")
    @Operation(
            summary = "VK Service Auth with User ID",
            description = "Авторизация через сервисный ключ VK с указанием ID пользователя")
    public ResponseEntity<JwtResponse> authenticateWithVkServiceAndUserId(
            @Valid @RequestBody VkServiceLoginRequest request) {
        log.info("Получен запрос авторизации VK для пользователя ID: {}", request.getUserId());

        ResponseEntity<JwtResponse> response;
        if (enableServiceLogin) {
            try {
                log.debug("Сервисный логин включен, выполняю авторизацию для VK пользователя: {}", request.getUserId());
                JwtResponse jwtResponse = vkAuthService.loginWithVkServiceKeyAndUserId(request.getUserId());

                log.info(
                        "Авторизация VK успешна для пользователя ID: {}, логин: {}",
                        request.getUserId(),
                        jwtResponse.getLogin());
                log.debug(
                        "JWT токен для пользователя {}: {}",
                        jwtResponse.getLogin(),
                        jwtResponse
                                        .getToken()
                                        .substring(
                                                0,
                                                Math.min(
                                                        20,
                                                        jwtResponse.getToken().length())) + "...");

                response = ResponseEntity.ok(jwtResponse);
            } catch (Exception e) {
                log.error("Ошибка аутентификации для пользователя VK ID: {}", request.getUserId(), e);
                response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } else {
            log.warn("Сервисный логин отключен, отклоняю запрос для VK пользователя: {}", request.getUserId());
            response = ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return response;
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход из системы", description = "Завершает сеанс пользователя и удаляет refresh токен")
    public ResponseEntity<MessageResponse> logoutUser() {
        String username = null;
        Object principal =
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        }
        MessageResponse response;
        if (username != null) {
            response = authService.logout(username);
        } else {
            response = new MessageResponse("Вы не авторизованы");
        }
        return ResponseEntity.ok(response);
    }
}
