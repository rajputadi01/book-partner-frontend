package com.capg.portal.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;

@Controller
public class AuthMvcController {

    private final RestClient restClient;

    public AuthMvcController(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("http://localhost:8080").build();
    }

    @GetMapping("/login")
    public String showLogin() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegister() {
        return "auth/register";
    }

    @PostMapping("/register/save")
    public String executeRegister(@RequestParam("username") String username, 
                                  @RequestParam("password") String password, 
                                  RedirectAttributes ra) {
        try {
            restClient.post()
                    .uri("/api/auth/register")
                    .body(Map.of("username", username, "password", password))
                    .retrieve()
                    .toBodilessEntity();
                    
            ra.addFlashAttribute("successMessage", "Account created! You can now log in.");
            return "redirect:/login";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Registration failed. Username may already exist.");
            return "redirect:/register";
        }
    }
}