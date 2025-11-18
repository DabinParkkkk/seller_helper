package com.example.seller_helper.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
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

    // =========================
    //  한진 변환
    // =========================
    public ByteArrayInputStream convertHanjin(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // 1) 데이터 행 수집 (제목 제외 + 완전 빈 행 제거)
        List<Row> dataRows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (!isEmptyRow(row)) {
                dataRows.add(row);
            }
        }

        // 2) 정렬: M열(String) → W열(Number)
        //    ● 1순위: M열(12번 인덱스) → 문자열 오름차순
        //    ● 2순위: W열(22번 인덱스) → 숫자 오름차순
        dataRows.sort(
            Comparator
                .comparing((Row r) -> getCellStringValue(r.getCell(12)))
                .thenComparing( r -> getCellNumericValue(r.getCell(22)) )
        );

        // 3) 결과 Workbook / Sheet 생성
        XSSFWorkbook outWorkbook = new XSSFWorkbook();
        Sheet outSheet = outWorkbook.createSheet("Hanjin_Output");

        // 4) 헤더 작성 (네가 쓰던 헤더 유지)
        Row header = outSheet.createRow(0);
        header.createCell(0).setCellValue("수하인명");
        header.createCell(1).setCellValue("연락처");
        header.createCell(4).setCellValue("우편번호");
        header.createCell(5).setCellValue("주소");
        header.createCell(6).setCellValue("수량");
        header.createCell(7).setCellValue("상품명");
        header.createCell(11).setCellValue("요청사항");

        // 5) 본문 데이터 (2행부터 정확히 시작)
        for (int i = 0; i < dataRows.size(); i++) {
            Row src = dataRows.get(i);
            if (src == null) continue;

            Row dst = outSheet.createRow(i + 1); // 헤더 0행, 데이터 1행부터(=엑셀 2번째 줄)

            // 매핑: 원본 → 새 시트
            copyCell(src, dst, 12, 7);   // M -> H (여기 지금은 7열. 헤더는 '상품명'이라 되어 있으니 나중에 네가 맞춰서 조정)
            copyCell(src, dst, 22, 6);   // W -> G
            copyCell(src, dst, 26, 0);   // AA -> A
            copyCell(src, dst, 27, 1);   // AB -> B
            copyCell(src, dst, 28, 4);   // AC -> E
            copyCell(src, dst, 29, 5);   // AD -> F
            copyCell(src, dst, 30, 11);  // AE -> L
        }

        // 6) 헤더 Bold 스타일
        CellStyle headerStyle = outWorkbook.createCellStyle();
        Font boldFont = outWorkbook.createFont();
        boldFont.setBold(true);
        headerStyle.setFont(boldFont);
        for (Cell cell : header) {
            cell.setCellStyle(headerStyle);
        }

        // 7) 열 너비 자동 조정
        for (int c = 0; c <= 11; c++) {
            outSheet.autoSizeColumn(c);
        }

        // 8) 메모리 스트림으로 내보내기
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        outWorkbook.write(out);
        outWorkbook.close();
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // =========================
    //  CJ 변환 (현재는 한진과 동일하게 둠)
    //  나중에 이 메소드 안만 수정하면 됨
    // =========================
    public ByteArrayInputStream convertCj(MultipartFile file) throws IOException {
        // 우선은 한진이랑 똑같이 처리시켜두고,
        // 나중에 CJ 전용 헤더/매핑/정렬 로직만 여기서 바꾸면 됨.
        return convertHanjin(file);
    }

    // =========================
    //  사용자 설정 변환
    //  mappingJson : {"name":"AA","phone":"AB", ...} 형식
    // =========================
    public ByteArrayInputStream convertWithCustomMapping(MultipartFile file, String mappingJson) throws IOException {
        CustomMapping mapping = objectMapper.readValue(mappingJson, CustomMapping.class);

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        List<Row> dataRows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (!isEmptyRow(row)) {
                dataRows.add(row);
            }
        }

        // 여기서는 정렬을 안 하거나,
        // 원하면 mapping.product 열 기준으로 정렬하도록 바꿔도 됨.
        // 일단 예시는 "정렬 없이 순서대로" 넣자:
        // (상품명 기준 정렬하고 싶으면 columnLetterToIndex(mapping.product) 써서 정렬 로직 추가하면 됨)

        XSSFWorkbook outWorkbook = new XSSFWorkbook();
        Sheet outSheet = outWorkbook.createSheet("Custom_Output");

        // 헤더: 한진과 비슷하게 고정 (필요하면 여기도 mapping에 따라 바꿔도 됨)
        Row header = outSheet.createRow(0);
        header.createCell(0).setCellValue("수하인명");
        header.createCell(1).setCellValue("연락처");
        header.createCell(4).setCellValue("우편번호");
        header.createCell(5).setCellValue("주소");
        header.createCell(6).setCellValue("수량");
        header.createCell(7).setCellValue("상품명");
        header.createCell(11).setCellValue("요청사항");

        int nameCol     = columnLetterToIndex(mapping.name);
        int phoneCol    = columnLetterToIndex(mapping.phone);
        int postcodeCol = columnLetterToIndex(mapping.postcode);
        int addressCol  = columnLetterToIndex(mapping.address);
        int qtyCol      = columnLetterToIndex(mapping.qty);
        int productCol  = columnLetterToIndex(mapping.product);
        int requestCol  = columnLetterToIndex(mapping.request);

        for (int i = 0; i < dataRows.size(); i++) {
            Row src = dataRows.get(i);
            if (src == null) continue;

            Row dst = outSheet.createRow(i + 1);

            copyCell(src, dst, nameCol, 0);     // 수하인명
            copyCell(src, dst, phoneCol, 1);    // 연락처
            copyCell(src, dst, postcodeCol, 4); // 우편번호
            copyCell(src, dst, addressCol, 5);  // 주소
            copyCell(src, dst, qtyCol, 6);      // 수량
            copyCell(src, dst, productCol, 7);  // 상품명
            copyCell(src, dst, requestCol, 11); // 요청사항
        }

        CellStyle headerStyle = outWorkbook.createCellStyle();
        Font boldFont = outWorkbook.createFont();
        boldFont.setBold(true);
        headerStyle.setFont(boldFont);
        for (Cell cell : header) {
            cell.setCellStyle(headerStyle);
        }

        for (int c = 0; c <= 11; c++) {
            outSheet.autoSizeColumn(c);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        outWorkbook.write(out);
        outWorkbook.close();
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // =========================
    //  유틸들
    // =========================

    // 완전히 빈 행인지 체크
    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell == null) continue;
            if (cell.getCellType() == CellType.BLANK) continue;
            if (cell.getCellType() == CellType.STRING &&
                cell.getStringCellValue().trim().isEmpty()) continue;
            // 여기까지 안 걸리면 뭔가 값이 있는 셀
            return false;
        }
        return true;
    }

    // 셀 복사
    private void copyCell(Row fromRow, Row toRow, int fromCol, int toCol) {
        if (fromCol < 0) return; // 사용자 설정에서 빈 값 들어온 경우 방어
        Cell fromCell = fromRow.getCell(fromCol);
        if (fromCell == null) return;
        Cell toCell = toRow.createCell(toCol);

        switch (fromCell.getCellType()) {
            case STRING -> toCell.setCellValue(fromCell.getStringCellValue());
            case NUMERIC -> toCell.setCellValue(fromCell.getNumericCellValue());
            case BOOLEAN -> toCell.setCellValue(fromCell.getBooleanCellValue());
            default -> toCell.setCellValue(fromCell.toString());
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> cell.toString();
        };
    }

    private double getCellNumericValue(Cell cell) {
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    yield 0;
                }
            }
            default -> 0;
        };
    }

    // 엑셀 열 문자(A, B, ..., Z, AA, AB, ...) → 0-based 인덱스
    private int columnLetterToIndex(String col) {
        if (col == null || col.isBlank()) return -1;
        String s = col.trim().toUpperCase();
        int result = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch < 'A' || ch > 'Z') return -1;
            result = result * 26 + (ch - 'A' + 1);
        }
        return result - 1; // A=0, B=1, ..., Z=25, AA=26 ...
    }

    // 사용자 매핑용 DTO
    public static class CustomMapping {
        public String name;
        public String phone;
        public String postcode;
        public String address;
        public String qty;
        public String product;
        public String request;

        public CustomMapping() {} // Jackson용 기본 생성자
    }


// =========================
//  쿠팡 + 한진 매칭 기능
// =========================
public ByteArrayInputStream matchHanjinWithCoupang(
        File coupangFile, MultipartFile hanjinFile) throws IOException, InvalidFormatException {

    Workbook coupangWb = new XSSFWorkbook(coupangFile);
    Sheet coupangSheet = coupangWb.getSheetAt(0);

    Workbook hanjinWb = new XSSFWorkbook(hanjinFile.getInputStream());
    Sheet hanjinSheet = hanjinWb.getSheetAt(0);

    Map<String, String> hanjinMap = new HashMap<>();

    // 한진 파일 읽기
    for (Row row : hanjinSheet) {
        if (row.getRowNum() == 0) continue;

        String name = getCellStringValue(row.getCell(19));  // T열 index 19
        String product = getCellStringValue(row.getCell(41)); // AP열 index 41
        String zipcode = getCellStringValue(row.getCell(25)); // Z열 index 25
        String tracking = getCellStringValue(row.getCell(3)); // D열 index 3

        if (name.isBlank() || product.isBlank() || zipcode.isBlank()) continue;

        String key = name + "|" + product + "|" + zipcode;
        hanjinMap.put(key, tracking);
    }

    // 쿠팡 파일에 운송장번호 삽입
    for (Row row : coupangSheet) {
        if (row.getRowNum() == 0) continue;

        String name = getCellStringValue(row.getCell(26));   // AA
        String product = getCellStringValue(row.getCell(12)); // M
        String zipcode = getCellStringValue(row.getCell(28)); // AC

        String key = name + "|" + product + "|" + zipcode;

        if (hanjinMap.containsKey(key)) {
            Cell cell = row.getCell(4); // E열
            if (cell == null) cell = row.createCell(4);
            cell.setCellValue(hanjinMap.get(key));
        }
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    coupangWb.write(out);

    coupangWb.close();
    hanjinWb.close();

    return new ByteArrayInputStream(out.toByteArray());
}
}