package com.example.seller_helper.mapping;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMappingRepository extends JpaRepository<UserMapping, Long> {
    UserMapping findByUserId(Long userId);
}