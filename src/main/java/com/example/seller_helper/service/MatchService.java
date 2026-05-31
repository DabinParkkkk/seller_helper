package com.example.seller_helper.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    public ByteArrayInputStream matchHanjin(
            MultipartFile coupangFile,
            MultipartFile hanjinFile,
            String invoiceCol
    ) throws IOException {

        Workbook coupangWb = new XSSFWorkbook(coupangFile.getInputStream());
        Workbook hanjinWb = new XSSFWorkbook(hanjinFile.getInputStream());

        Sheet coupangSheet = coupangWb.getSheetAt(0);
        Sheet hanjinSheet = hanjinWb.getSheetAt(0);

        int invoiceColIdx = column(invoiceCol); // 사용자가 지정한 운송장 열
        int matchCount = 0;

        // 순서대로 택배사 출력파일 운송장열 → 쿠팡 E열(4) 복붙
        for (int i = 1; i <= hanjinSheet.getLastRowNum(); i++) {
            Row hanjinRow = hanjinSheet.getRow(i);
            Row coupangRow = coupangSheet.getRow(i);

            if (hanjinRow == null || coupangRow == null) continue;

            Cell invoiceCell = hanjinRow.getCell(invoiceColIdx);
            if (invoiceCell == null) continue;

            String invoice = getString(invoiceCell);
            if (invoice.isBlank()) continue;

            Cell targetCell = coupangRow.getCell(4); // 쿠팡 E열 고정
            if (targetCell == null) targetCell = coupangRow.createCell(4);
            targetCell.setCellValue(invoice);
            matchCount++;
        }

        log.info("운송장 입력 완료: {}건", matchCount);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        coupangWb.write(out);
        coupangWb.close();
        hanjinWb.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    private String getString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> cell.toString().trim();
        };
    }

    private int column(String col) {
        if (col == null || col.isBlank()) return 3; // 기본값 D열
        col = col.trim().toUpperCase();
        int result = 0;
        for (int i = 0; i < col.length(); i++) {
            result = result * 26 + (col.charAt(i) - 'A' + 1);
        }
        return result - 1;
    }
}