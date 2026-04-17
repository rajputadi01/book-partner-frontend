package com.capg.portal.frontend.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;

@ControllerAdvice
public class FrontendExceptionHandler {

    // 1. Catch Server Offline Errors (If Laptop B is turned off or disconnected from Wi-Fi)
    @ExceptionHandler({ConnectException.class, ResourceAccessException.class})
    public String handleConnectionError(Exception ex, Model model) {
    	ex.printStackTrace();
        model.addAttribute("errorMessage", "Cannot connect to the Backend Server. Please ensure the backend API (10.30.74.116) is running.");
        return "error"; 
    }

    // 2. Catch Backend Data Errors (e.g., 404 Not Found, 400 Bad Request returned by the REST API)
    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    public String handleApiErrors(Exception ex, Model model) {
    	ex.printStackTrace();
        model.addAttribute("errorMessage", "The Backend Server rejected the request: " + ex.getMessage());
        return "error";
    }

    // 3. Catch generic internal UI crashes
    @ExceptionHandler(Exception.class)
    public String handleGenericError(Exception ex, Model model) {
    	ex.printStackTrace();
        model.addAttribute("errorMessage", "An unexpected error occurred in the UI: " + ex.getMessage());
        return "error";
    }
}