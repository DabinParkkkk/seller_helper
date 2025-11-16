package com.example.seller_helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/convert")
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    // ✅ 한진
    @PostMapping("/hanjin")
    public ResponseEntity<byte[]> convertHanjin(@RequestParam("file") MultipartFile file) throws IOException {
        ByteArrayInputStream converted = excelService.convertHanjin(file);
        byte[] bytes = converted.readAllBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hanjin_output.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    // ✅ CJ
    @PostMapping("/cj")
    public ResponseEntity<byte[]> convertCj(@RequestParam("file") MultipartFile file) throws IOException {
        ByteArrayInputStream converted = excelService.convertCj(file);
        byte[] bytes = converted.readAllBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cj_output.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    // ✅ 사용자 설정
    @PostMapping("/custom")
    public ResponseEntity<byte[]> convertCustom(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mapping") String mappingJson
    ) throws IOException {

        ByteArrayInputStream converted = excelService.convertWithCustomMapping(file, mappingJson);
        byte[] bytes = converted.readAllBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=custom_output.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
