package com.example.seller_helper.controller;
//
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.seller_helper.service.MatchService;

@RestController
@RequestMapping("/match")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @SuppressWarnings("null")
    @PostMapping("/hanjin")
    public ResponseEntity<byte[]> matchHanjin(
            @RequestParam("coupang") MultipartFile coupangFile,
            @RequestParam("hanjin") MultipartFile hanjinFile
    ) throws IOException {

        ByteArrayInputStream result = matchService.matchHanjin(coupangFile, hanjinFile);
        byte[] bytes = result.readAllBytes();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=matched_output.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
