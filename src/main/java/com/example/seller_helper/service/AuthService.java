package com.example.seller_helper.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // 회원가입
    public String signup(String email, String password) {

    if (userRepository.findByEmail(email).isPresent()) {
        return "이미 존재하는 이메일입니다.";
    }

    User u = new User();
    u.setEmail(email);
    u.setPassword(passwordEncoder.encode(password));
    userRepository.save(u);

    return "success";
}


    // 로그인
    public String login(String email, String rawPassword) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null)
            return "이메일이 존재하지 않습니다.";

        if (!passwordEncoder.matches(rawPassword, user.getPassword()))
            return "비밀번호가 일치하지 않습니다.";

        return jwtUtil.generateToken(email); // JWT 발급
    }
}
