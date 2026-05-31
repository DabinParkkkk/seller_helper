package com.example.seller_helper.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.seller_helper.common.ApiResponse;
import com.example.seller_helper.controller.request.LoginRequest;
import com.example.seller_helper.controller.request.SignupRequest;
import com.example.seller_helper.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<String> signup(@RequestBody @Valid SignupRequest req) {
        String result = authService.signup(req.getEmail(), req.getPassword());
        return ApiResponse.ok(result);
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody @Valid LoginRequest req) {
        return ApiResponse.ok(authService.login(req.getEmail(), req.getPassword()));
    }
}