package com.hmtravels.agency.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalWebExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleConflict(HttpServletRequest request, RedirectAttributes ra) {
        // This detects which page the user was on
        String referer = request.getHeader("Referer");

        // Custom messages based on where the error happened
        String errorMessage = "Operation failed: This record (CNIC, Reg No, or Name) already exists in our system.";

        ra.addFlashAttribute("error", errorMessage);

        // Redirects them back to exactly where they were
        return "redirect:" + referer;
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralError(HttpServletRequest request, RedirectAttributes ra) {
        String referer = request.getHeader("Referer");
        ra.addFlashAttribute("error", "Something went wrong. Please check your inputs and try again.");
        return "redirect:" + referer;
    }
}