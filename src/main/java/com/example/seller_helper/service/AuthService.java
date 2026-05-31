package com.example.seller_helper.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.seller_helper.common.AuthException;
import com.example.seller_helper.security.JwtUtil;
import com.example.seller_helper.user.User;
import com.example.seller_helper.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String signup(String email, String password) {
        if (userRepository.findByEmail(email).isPresent())
            throw new AuthException("이미 존재하는 이메일입니다.", HttpStatus.CONFLICT);
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        userRepository.save(u);
        return "회원가입이 완료되었습니다.";
    }

    public String login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("이메일이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(rawPassword, user.getPassword()))
            throw new AuthException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        return jwtUtil.generateToken(email);
    }
}