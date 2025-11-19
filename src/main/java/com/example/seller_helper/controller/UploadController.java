package com.example.seller_helper.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.seller_helper.service.ExcelService;

@RestController
@RequestMapping("/excel")
public class UploadController {

    private final ExcelService excelService;

    public UploadController(ExcelService excelService) {
        this.excelService = excelService;
    }

    // =========================
    //  정규 변환 (한진 / CJ)
    // =========================
@PostMapping("/convert")
public ResponseEntity<InputStreamResource> convert(
        @RequestParam("file") MultipartFile file,
        @RequestParam("type") String type) throws IOException {

    ByteArrayInputStream result;

    if (type.equals("hanjin")) {
        result = excelService.convertHanjin(file);
    } else if (type.equals("cj")) {
        result = excelService.convertCj(file);
    } else {
        return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(new InputStreamResource(result));
}

    // =========================
    //  사용자 매핑 변환
    // =========================
    @PostMapping("/custom")
    public ResponseEntity<InputStreamResource> customConvert(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mapping") String mappingJson) throws IOException {

        ByteArrayInputStream result = excelService.convertWithCustomMapping(file, mappingJson);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=custom.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(result));
    }

    // =========================
    //  쿠팡 + 한진 매칭 기능
    // =========================
    @PostMapping("/match")
    public ResponseEntity<InputStreamResource> matchFiles(
            @RequestParam("coupangFile") MultipartFile coupangFile,
            @RequestParam("hanjinFile") MultipartFile hanjinFile)
            throws IOException, InvalidFormatException {

        // 쿠팡 파일은 File 객체로 변환이 필요함c
        File temp = File.createTempFile("coupang", ".xlsx");
        coupangFile.transferTo(temp);

        ByteArrayInputStream result =
                excelService.matchHanjinWithCoupang(temp, hanjinFile);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=matched.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(result));
    }
}
