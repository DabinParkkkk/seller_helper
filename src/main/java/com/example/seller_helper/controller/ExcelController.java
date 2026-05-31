package com.example.seller_helper.controller;

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
import com.example.seller_helper.service.ExcelService.ConvertResult;

@RestController
@RequestMapping("/excel")
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @PostMapping("/hanjin")
    public ResponseEntity<InputStreamResource> convertHanjin(
            @RequestParam("file") MultipartFile file) throws Exception {
        ConvertResult result = excelService.convertHanjin(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hanjin.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(result.hanjinFile));
    }

    @PostMapping("/hanjin/coupang-sorted")
    public ResponseEntity<InputStreamResource> convertHanjinCoupangSorted(
            @RequestParam("file") MultipartFile file) throws Exception {
        ConvertResult result = excelService.convertHanjin(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=coupang_sorted.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(result.coupangSortedFile));
    }

    @PostMapping("/custom")
    public ResponseEntity<InputStreamResource> convertCustom(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mappingJson") String mappingJson) throws Exception {
        ConvertResult result = excelService.convertWithCustomMapping(file, mappingJson);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=custom_output.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(result.hanjinFile));
    }

    @PostMapping("/custom/coupang-sorted")
    public ResponseEntity<InputStreamResource> convertCustomCoupangSorted(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mappingJson") String mappingJson) throws Exception {
        ConvertResult result = excelService.convertWithCustomMapping(file, mappingJson);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=coupang_sorted.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(result.coupangSortedFile));
    }
}