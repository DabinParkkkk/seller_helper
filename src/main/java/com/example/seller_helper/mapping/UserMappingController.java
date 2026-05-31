package com.example.seller_helper.mapping;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.seller_helper.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mapping")
@RequiredArgsConstructor
public class UserMappingController {

    private final UserMappingService service;
    private final UserRepository userRepo;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저 없음"))
                .getId();
    }

    @PostMapping("/save")
    public String save(@RequestBody UserMapping dto) {
        service.save(getCurrentUserId(), dto);
        return "saved";
    }

    @GetMapping("/load")
    public UserMapping load() {
        return service.load(getCurrentUserId());
    }
}