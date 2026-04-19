package com.capg.portal.frontend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class FrontendSecurityConfig {

    private final BackendAuthenticationProvider authProvider;

    public FrontendSecurityConfig(BackendAuthenticationProvider authProvider) {
        this.authProvider = authProvider;
    }

    // FIX: Define the AuthenticationManager explicitly to prevent Spring from erasing the password
    @Bean
    public AuthenticationManager authenticationManager() {
        ProviderManager providerManager = new ProviderManager(authProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false); 
        return providerManager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationManager(authenticationManager()) // Inject the custom manager here
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/register/save", "/css/**", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .permitAll()
            );

        return http.build();
    }
}