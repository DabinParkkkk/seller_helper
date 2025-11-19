package com.example.seller_helper.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.seller_helper.controller.request.LoginRequest;
import com.example.seller_helper.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

   @PostMapping("/signup")
public String signup(@RequestParam String email,
                     @RequestParam String password) {
    return authService.signup(email, password);
}


@PostMapping("/login")
public String login(@RequestBody LoginRequest req) {
    return authService.login(req.getEmail(), req.getPassword());
}

}
