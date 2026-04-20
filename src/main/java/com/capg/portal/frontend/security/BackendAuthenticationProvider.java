package com.capg.portal.frontend.security;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class BackendAuthenticationProvider implements AuthenticationProvider {

    private final RestClient restClient;

    public BackendAuthenticationProvider(RestClient.Builder restClientBuilder) {
        // We create a temporary client just for auth validation (no interceptor to prevent loops)
        this.restClient = restClientBuilder.baseUrl("http://localhost:8080").build(); 
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            String encodedCreds = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            
            // Try to access the backend
            JsonNode response = restClient.get()
                    .uri("/api/auth/me")
                    .header("Authorization", "Basic " + encodedCreds)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) 
            {
                throw new AuthenticationServiceException("Empty response from backend.");
            }
            
            // Extract Roles from the backend response
            List<GrantedAuthority> authorities = new ArrayList<>();
            response.get("authorities").forEach(authNode -> {
                authorities.add(new SimpleGrantedAuthority(authNode.get("authority").asText()));
            });

            // Return full token. Password MUST be passed so the Interceptor can use it later!
            return new UsernamePasswordAuthenticationToken(username, password, authorities);
            
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new BadCredentialsException("Invalid username or password.");
        } catch (Exception e) {
            throw new AuthenticationServiceException("Backend authentication service is down.", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}