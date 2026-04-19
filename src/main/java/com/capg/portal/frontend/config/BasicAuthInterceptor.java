package com.capg.portal.frontend.config;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Base64;

public class BasicAuthInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // If user is logged into the frontend, attach their credentials to the backend request!
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            String password = auth.getCredentials().toString(); // Requires eraseCredentials(false) in config
            
            String encoded = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            request.getHeaders().add("Authorization", "Basic " + encoded);
        }
        return execution.execute(request, body);
    }
}