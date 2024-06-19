package com.example.demo.controller;


import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.LinkedInLoginService;


@RestController
public class LinkedInController {

    private final LinkedInLoginService loginService;

    public LinkedInController(LinkedInLoginService loginService) {
        this.loginService = loginService;
    }

    @GetMapping("/login-linkedin")
    public String loginToLinkedIn() {
        loginService.loginToLinkedIn();
        return "LinkedIn login process initiated";
    }
    @GetMapping("/login")
    public String getMethodName() {
        return "mg";
    }

    @GetMapping("/ping")
    public String ping() {
        if (loginService.isSessionActive()) {
            return "Session is active";
        } else {
            return "Session is inactive";
        }
    }

    @GetMapping("/fetchFeed")
    public List<String> fetchFeed() {
        return loginService.fetchFeed();
    }

    @GetMapping("/endSession")
    public String endSession() {
        loginService.endSession();
        return "Session ended";
    }
    
}
