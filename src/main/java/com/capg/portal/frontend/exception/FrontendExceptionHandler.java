package com.capg.portal.frontend.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import java.net.ConnectException;

@ControllerAdvice
public class FrontendExceptionHandler {

    // 1. Server Offline
    @ExceptionHandler({ConnectException.class, ResourceAccessException.class})
    public String handleConnectionError(Exception ex, Model model) {
        model.addAttribute("status", "503");
        model.addAttribute("error", "Service Unavailable");
        model.addAttribute("message", "Cannot connect to the Backend Server. Please ensure the backend API is running.");
        return "error"; 
    }

    // 2. Catch Backend 4xx and 5xx Errors (404, 409, 400)
    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    public String handleApiErrors(HttpStatusCodeException ex, Model model) {
        String cleanMessage = "An error occurred processing your request.";
        try {
            // Extract the clean "message" string from the Backend's ErrorResponse JSON!
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(ex.getResponseBodyAsString());
            if (node.has("message")) {
                cleanMessage = node.get("message").asText();
            }
        } catch (Exception e) {
            cleanMessage = ex.getMessage();
        }

        model.addAttribute("status", ex.getStatusCode().value());
        model.addAttribute("error", ex.getStatusText());
        model.addAttribute("message", cleanMessage);
        return "error";
    }

    // 3. Generic UI Crashes
    @ExceptionHandler(Exception.class)
    public String handleGenericError(Exception ex, Model model) {
        model.addAttribute("status", "500");
        model.addAttribute("error", "Frontend Error");
        model.addAttribute("message", "An unexpected error occurred in the UI: " + ex.getMessage());
        return "error";
    }
}