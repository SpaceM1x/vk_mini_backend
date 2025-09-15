package com.hackaton.vk_mini_backend.security.jwt;

import com.hackaton.vk_mini_backend.config.security.jwt.AuthEntryPointJwt;
import com.hackaton.vk_mini_backend.config.security.jwt.AuthTokenFilter;
import com.hackaton.vk_mini_backend.exception.SecurityConfigException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authConfig) {
        try {
            return authConfig.getAuthenticationManager();
        } catch (Exception e) {
            throw new SecurityConfigException("Ошибка при получении AuthenticationManager", e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Разрешаем локальную разработку
        configuration.addAllowedOrigin("http://localhost:4200");

        // Разрешаем все домены VK Mini Apps
        configuration.addAllowedOriginPattern("https://*.tunnel.vk-apps.com");
        configuration.addAllowedOriginPattern("https://*.vk-apps.com");
        configuration.addAllowedOriginPattern("https://vk.com");
        configuration.addAllowedOriginPattern("https://*.vk.com");

        // Разрешаем методы HTTP
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedMethod("PATCH");

        // Разрешаем все заголовки
        configuration.addAllowedHeader("*");

        // Разрешаем отправку cookies и авторизационных заголовков
        configuration.setAllowCredentials(true);

        // Разрешаем предварительные запросы на 24 часа
        configuration.setMaxAge(86400L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) {
        try {
            http.csrf(AbstractHttpConfigurer::disable)
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**")
                            .permitAll()
                            .requestMatchers("/api/auth/login/vk-service-user")
                            .permitAll()
                            .requestMatchers("/api/public/**")
                            .permitAll()
                            .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                            .permitAll()
                            .requestMatchers("/h2-console/**", "/h2-console/", "/h2-console")
                            .permitAll()
                            .anyRequest()
                            .authenticated());

            http.authenticationProvider(authenticationProvider());
            http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

            return http.build();
        } catch (Exception e) {
            throw new SecurityConfigException("Ошибка при настройке SecurityFilterChain", e);
        }
    }
}
