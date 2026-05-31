package com.example.seller_helper.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ExcelService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class ConvertResult {
        public ByteArrayInputStream hanjinFile;
        public ByteArrayInputStream coupangSortedFile;

        public ConvertResult(ByteArrayInputStream hanjinFile, ByteArrayInputStream coupangSortedFile) {
            this.hanjinFile = hanjinFile;
            this.coupangSortedFile = coupangSortedFile;
        }
    }

    // =========================
    //  한진 변환
    // =========================
    public ConvertResult convertHanjin(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            List<Row> dataRows = collectRows(sheet);
            dataRows.sort(
                Comparator
                    .comparing((Row r) -> getCellStringValue(r.getCell(12)))
                    .thenComparing(r -> getCellNumericValue(r.getCell(22)))
            );

            byte[] hanjinBytes;
            try (XSSFWorkbook hanjinWb = new XSSFWorkbook();
                 ByteArrayOutputStream hanjinOut = new ByteArrayOutputStream()) {
                Sheet hanjinSheet = hanjinWb.createSheet("Hanjin_Output");

                Row header = hanjinSheet.createRow(0);
                header.createCell(0).setCellValue("수하인명");
                header.createCell(1).setCellValue("연락처");
                header.createCell(4).setCellValue("우편번호");
                header.createCell(5).setCellValue("주소");
                header.createCell(6).setCellValue("수량");
                header.createCell(7).setCellValue("상품명");
                header.createCell(11).setCellValue("요청사항");

                for (int i = 0; i < dataRows.size(); i++) {
                    Row src = dataRows.get(i);
                    if (src == null) continue;
                    Row dst = hanjinSheet.createRow(i + 1);
                    copyCell(src, dst, 12, 7);
                    copyCell(src, dst, 22, 6);
                    copyCell(src, dst, 26, 0);
                    copyCell(src, dst, 27, 1);
                    copyCell(src, dst, 28, 4);
                    copyCell(src, dst, 29, 5);
                    copyCell(src, dst, 30, 11);
                }

                applyBoldHeader(hanjinWb, header);
                for (int c = 0; c <= 11; c++) hanjinSheet.autoSizeColumn(c);

                hanjinWb.write(hanjinOut);
                hanjinBytes = hanjinOut.toByteArray();
            }

            byte[] coupangBytes = buildSortedCoupangBytes(sheet, dataRows);

            return new ConvertResult(
                new ByteArrayInputStream(hanjinBytes),
                new ByteArrayInputStream(coupangBytes)
            );
        }
    }

    // =========================
    //  커스텀 매핑 변환
    // =========================
    public ConvertResult convertWithCustomMapping(MultipartFile file, String mappingJson) throws IOException {
        CustomMapping mapping = objectMapper.readValue(mappingJson, CustomMapping.class);

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            List<Row> dataRows = collectRows(sheet);
            int productColIdx = columnLetterToIndex(mapping.product);
            dataRows.sort(Comparator.comparing((Row r) -> getCellStringValue(r.getCell(productColIdx))));

            int nameCol     = columnLetterToIndex(mapping.name);
            int phoneCol    = columnLetterToIndex(mapping.phone);
            int postcodeCol = columnLetterToIndex(mapping.postcode);
            int addressCol  = columnLetterToIndex(mapping.address);
            int qtyCol      = columnLetterToIndex(mapping.qty);
            int requestCol  = columnLetterToIndex(mapping.request);

            byte[] customBytes;
            try (XSSFWorkbook customWb = new XSSFWorkbook();
                 ByteArrayOutputStream customOut = new ByteArrayOutputStream()) {
                Sheet customSheet = customWb.createSheet("Custom_Output");

                Row header = customSheet.createRow(0);
                header.createCell(0).setCellValue("수하인명");
                header.createCell(1).setCellValue("연락처");
                header.createCell(4).setCellValue("우편번호");
                header.createCell(5).setCellValue("주소");
                header.createCell(6).setCellValue("수량");
                header.createCell(7).setCellValue("상품명");
                header.createCell(11).setCellValue("요청사항");

                for (int i = 0; i < dataRows.size(); i++) {
                    Row src = dataRows.get(i);
                    if (src == null) continue;
                    Row dst = customSheet.createRow(i + 1);
                    copyCell(src, dst, nameCol, 0);
                    copyCell(src, dst, phoneCol, 1);
                    copyCell(src, dst, postcodeCol, 4);
                    copyCell(src, dst, addressCol, 5);
                    copyCell(src, dst, qtyCol, 6);
                    copyCell(src, dst, productColIdx, 7);
                    copyCell(src, dst, requestCol, 11);
                }

                applyBoldHeader(customWb, header);
                for (int c = 0; c <= 11; c++) customSheet.autoSizeColumn(c);

                customWb.write(customOut);
                customBytes = customOut.toByteArray();
            }

            byte[] coupangBytes = buildSortedCoupangBytes(sheet, dataRows);

            return new ConvertResult(
                new ByteArrayInputStream(customBytes),
                new ByteArrayInputStream(coupangBytes)
            );
        }
    }

    // =========================
    //  유틸
    // =========================
    private List<Row> collectRows(Sheet sheet) {
        List<Row> rows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (!isEmptyRow(row)) rows.add(row);
        }
        return rows;
    }

    private byte[] buildSortedCoupangBytes(Sheet originalSheet, List<Row> sortedRows) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Coupang_Sorted");

            Row originalHeader = originalSheet.getRow(0);
            Row coupangHeader = sheet.createRow(0);
            for (int c = 0; c < originalHeader.getLastCellNum(); c++) {
                Cell src = originalHeader.getCell(c);
                Cell dst = coupangHeader.createCell(c);
                if (src != null) dst.setCellValue(getCellStringValue(src));
            }

            for (int i = 0; i < sortedRows.size(); i++) {
                Row srcRow = sortedRows.get(i);
                if (srcRow == null) continue;
                Row dstRow = sheet.createRow(i + 1);
                for (int c = 0; c < srcRow.getLastCellNum(); c++) {
                    Cell srcCell = srcRow.getCell(c);
                    Cell dstCell = dstRow.createCell(c);
                    if (srcCell == null) continue;
                    switch (srcCell.getCellType()) {
                        case STRING  -> dstCell.setCellValue(srcCell.getStringCellValue());
                        case NUMERIC -> dstCell.setCellValue(srcCell.getNumericCellValue());
                        case BOOLEAN -> dstCell.setCellValue(srcCell.getBooleanCellValue());
                        default      -> dstCell.setCellValue(srcCell.toString());
                    }
                }
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    private void applyBoldHeader(XSSFWorkbook wb, Row header) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        for (Cell cell : header) cell.setCellStyle(style);
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell == null) continue;
            if (cell.getCellType() == CellType.BLANK) continue;
            if (cell.getCellType() == CellType.STRING &&
                cell.getStringCellValue().trim().isEmpty()) continue;
            return false;
        }
        return true;
    }

    private void copyCell(Row fromRow, Row toRow, int fromCol, int toCol) {
        if (fromCol < 0) return;
        Cell fromCell = fromRow.getCell(fromCol);
        if (fromCell == null) return;
        Cell toCell = toRow.createCell(toCol);
        switch (fromCell.getCellType()) {
            case STRING  -> toCell.setCellValue(fromCell.getStringCellValue());
            case NUMERIC -> toCell.setCellValue(fromCell.getNumericCellValue());
            case BOOLEAN -> toCell.setCellValue(fromCell.getBooleanCellValue());
            default      -> toCell.setCellValue(fromCell.toString());
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default      -> cell.toString();
        };
    }

    private double getCellNumericValue(Cell cell) {
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING  -> {
                try { yield Double.parseDouble(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { yield 0; }
            }
            default -> 0;
        };
    }

    private int columnLetterToIndex(String col) {
        if (col == null || col.isBlank()) return -1;
        String s = col.trim().toUpperCase();
        int result = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch < 'A' || ch > 'Z') return -1;
            result = result * 26 + (ch - 'A' + 1);
        }
        return result - 1;
    }

    public static class CustomMapping {
        public String name;
        public String phone;
        public String postcode;
        public String address;
        public String qty;
        public String product;
        public String request;
        public CustomMapping() {}
    }
}
