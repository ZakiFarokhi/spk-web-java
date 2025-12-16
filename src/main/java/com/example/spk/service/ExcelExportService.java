package com.example.spk.service;

import com.example.spk.entity.Auditor;
import com.example.spk.entity.AuditorScore;
import com.example.spk.entity.Criteria;
import com.example.spk.entity.RankingResult;
import com.example.spk.entity.SubCriteria;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ExcelExportService {

    // --- UTILITY METHODS UNTUK STYLE ---
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDecimalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0.000000"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createNameStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        Cell cell = row.createCell(columnCount);
        if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value != null) {
            cell.setCellValue(value.toString());
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
    // ------------------------------------


    /** 1. Export Matriks Normalisasi Sub-Kriteria (r_ij) */
    public XSSFWorkbook exportNormalizationMatrix(List<Auditor> auditors, List<SubCriteria> allSubCriteria, Map<String, AuditorScore> normalizedScoreMap) {
        // TIDAK ADA try-with-resources di sini
        XSSFWorkbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Normalization Matrix (r_ij)");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle decimalStyle = createDecimalStyle(workbook);
        CellStyle nameStyle = createNameStyle(workbook);

        // HEADER ROW
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Auditor", headerStyle);

        int colNum = 1;
        for (SubCriteria sc : allSubCriteria) {
            String criteriaName = sc.getCriteria().getName();
            String subCriteriaPart = sc.getName().contains(" ") ? sc.getName().split(" ")[1] : sc.getName();
            String header = criteriaName + "." + subCriteriaPart;
            createCell(headerRow, colNum++, header, headerStyle);
        }

        // DATA ROWS
        int rowNum = 1;
        for (Auditor auditor : auditors) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, auditor.getName(), nameStyle);

            colNum = 1;
            for (SubCriteria sc : allSubCriteria) {
                String key = auditor.getId() + "_" + sc.getId();
                AuditorScore score = normalizedScoreMap.get(key);
                Double normalizedValue = (score != null && score.getNormalizedValue() != null) ? score.getNormalizedValue() : 0.0;

                createCell(row, colNum++, normalizedValue, decimalStyle);
            }
        }
        for (int i = 0; i < colNum; i++) { sheet.autoSizeColumn(i); }
        return workbook; // Dikembalikan terbuka
    }


    /** 2, 3, 4. Export Matriks Kriteria (C_j,norm, R_ij, V_ij) */
    public XSSFWorkbook exportCriteriaMatrix(List<Auditor> auditors, List<Criteria> criteriaList, Map<String, Double> dataMap, String sheetName) {
        // TIDAK ADA try-with-resources di sini
        XSSFWorkbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet(sheetName);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle decimalStyle = createDecimalStyle(workbook);
        CellStyle nameStyle = createNameStyle(workbook);

        // HEADER ROW
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Auditor", headerStyle);

        AtomicInteger colNum = new AtomicInteger(1);
        criteriaList.forEach(c ->
                createCell(headerRow, colNum.getAndIncrement(), c.getName(), headerStyle)
        );

        // DATA ROWS
        int rowNum = 1;
        for (Auditor auditor : auditors) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, auditor.getName(), nameStyle);

            colNum.set(1);
            criteriaList.forEach(c -> {
                String key = auditor.getId() + "_" + c.getId();
                Double score = dataMap.getOrDefault(key, 0.0);

                createCell(row, colNum.getAndIncrement(), score, decimalStyle);
            });
        }
        for (int i = 0; i < colNum.get(); i++) { sheet.autoSizeColumn(i); }
        return workbook; // Dikembalikan terbuka
    }

    /** 5. Export Hasil Ranking Akhir (Vi) */
    public XSSFWorkbook exportRankingResult(List<RankingResult> results) {
        // TIDAK ADA try-with-resources di sini
        XSSFWorkbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Final Ranking");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle decimalStyle = createDecimalStyle(workbook);

        // HEADER ROW
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Ranking", headerStyle);
        createCell(headerRow, 1, "Auditor", headerStyle);
        createCell(headerRow, 2, "Skor Preferensi (Vi)", headerStyle);

        // DATA ROWS
        int rowNum = 1;
        for (RankingResult result : results) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, rowNum - 1, createNameStyle(workbook));
            createCell(row, 1, result.getAuditor().getName(), createNameStyle(workbook));
            createCell(row, 2, result.getFinalScore(), decimalStyle);
        }

        for (int i = 0; i < 3; i++) { sheet.autoSizeColumn(i); }
        return workbook; // Dikembalikan terbuka
    }
}