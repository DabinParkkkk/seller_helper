package com.example.seller_helper.mapping;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserMappingService {

    private final UserMappingRepository repo;

    public UserMapping load(Long userId) {  
        return repo.findByUserId(userId);
    }

    public void save(Long userId, UserMapping mapping) { 
        mapping.setUserId(userId);
        repo.save(mapping);
    }
}