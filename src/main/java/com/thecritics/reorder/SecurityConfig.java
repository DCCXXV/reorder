package com.thecritics.reorder;

import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
	private Environment env;

    @Autowired
	private LoginSuccessHandler loginSuccessHandler;

    private static class KarateTestRequestMatcher implements RequestMatcher {
        @Override
        public boolean matches(HttpServletRequest request) {
            String testHeader = request.getHeader("X-Test-Framework");
            return "Karate".equals(testHeader);
        }
    }

    // configuración específica para desarrollo (H2 habilitado)
    @Bean
    @Profile("dev")
    @Order(1)
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        AntPathRequestMatcher h2PathMatcher = AntPathRequestMatcher.antMatcher("/h2/**");

        http
            .securityMatcher(h2PathMatcher)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(h2PathMatcher).permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(h2PathMatcher)
            )
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            );
        return http.build();
    }

    @Bean
    @Profile({ "dev", "prod" })
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login/**","/error", "/css/**", "/js/**", "/images/**", "/signup/**", "/search").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                // habilita CSRF normalmente con cookies
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // PERO ignora la protección CSRF para las solicitudes que coincidan con matcher de karate
                .ignoringRequestMatchers(new KarateTestRequestMatcher())
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/login")
                .permitAll()
				.successHandler(loginSuccessHandler)
            )
            .webAuthn((webAuthn) -> webAuthn
                .rpName("Spring Security Relying Party")
                .rpId("reorder.naivc.top")
                .allowedOrigins("https://reorder.naivc.top")
            )
            .exceptionHandling(exceptions -> exceptions
                 .accessDeniedPage("/error/403")
            );
        return http.build();
    }

    /**
     * Ver más información en
     * https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html#publish-authentication-manager-bean
     */
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService ordererDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(ordererDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
