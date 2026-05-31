package com.example.seller_helper.mapping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_mapping")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name_col") private String nameCol;
    @Column(name = "phone_col") private String phoneCol;
    @Column(name = "postcode_col") private String postcodeCol;
    @Column(name = "address_col") private String addressCol;
    @Column(name = "qty_col") private String qtyCol;
    @Column(name = "product_col") private String productCol;
    @Column(name = "request_col") private String requestCol;
}