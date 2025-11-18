package com.example.seller_helper.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MatchService {

    public ByteArrayInputStream matchHanjin(MultipartFile coupangFile, MultipartFile hanjinFile) throws IOException {

        Workbook coupangWb = new XSSFWorkbook(coupangFile.getInputStream());
        Workbook hanjinWb = new XSSFWorkbook(hanjinFile.getInputStream());

        Sheet coupangSheet = coupangWb.getSheetAt(0);
        Sheet hanjinSheet = hanjinWb.getSheetAt(0);

        // ============================
        // 1) 한진 데이터를 Map(T+AP+Z → D)
        // ============================
        Map<String, String> hanjinMap = new HashMap<>();

        for (int i = 1; i <= hanjinSheet.getLastRowNum(); i++) {

            Row row = hanjinSheet.getRow(i);
            if (row == null) continue;

            String name = getString(row.getCell(column("T")));
            String product = getString(row.getCell(column("AP")));
            String postcode = getString(row.getCell(column("Z")));
            String invoiceNum = getString(row.getCell(column("D")));

            if (name.isBlank() || product.isBlank() || postcode.isBlank()) continue;

            String key = name + "|" + product + "|" + postcode;
            hanjinMap.put(key, invoiceNum);
        }

        // ============================
        // 2) 쿠팡 파일 E열에 운송장 삽입
        // ============================
        for (int i = 1; i <= coupangSheet.getLastRowNum(); i++) {

            Row row = coupangSheet.getRow(i);
            if (row == null) continue;

            String name = getString(row.getCell(column("AA")));
            String product = getString(row.getCell(column("M")));
            String postcode = getString(row.getCell(column("AC")));

            String key = name + "|" + product + "|" + postcode;

            if (hanjinMap.containsKey(key)) {
                String invoice = hanjinMap.get(key);
                Cell targetCell = row.createCell(column("E"));
                targetCell.setCellValue(invoice);
            }
        }

        // ============================
        // 3) 결과 파일 스트림 변환
        // ============================
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        coupangWb.write(out);

        coupangWb.close();
        hanjinWb.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // Excel 문자열 안전 반환
    private String getString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> cell.toString().trim();
        };
    }

    // A→0, B→1, ..., Z→25, AA→26 변환
    private int column(String col) {
        col = col.trim().toUpperCase();
        int result = 0;
        for (int i = 0; i < col.length(); i++) {
            result = result * 26 + (col.charAt(i) - 'A' + 1);
        }
        return result - 1;
    }
}
