package com.example.spk.service;

import com.example.spk.entity.Crips;
import com.example.spk.entity.Criteria;
import com.example.spk.entity.SubCriteria;
import com.example.spk.repository.SubCriteriaRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class SubCriteriaService {

    private final SubCriteriaRepository subCriteriaRepository;
    private final CriteriaService criteriaService;

    public SubCriteriaService(SubCriteriaRepository subCriteriaRepository,
                              CriteriaService criteriaService) {
        this.subCriteriaRepository = subCriteriaRepository;
        this.criteriaService = criteriaService;
    }

    public List<SubCriteria> findAll() {
        return subCriteriaRepository.findAll();
    }

    public List<SubCriteria> findByCriteria(Long criteriaId) {
        return subCriteriaRepository.findByCriteriaId(criteriaId);
    }
    public List<SubCriteria> findByCriteriaId(Long criteriaId) {
        return subCriteriaRepository.findByCriteriaId(criteriaId);
    }

    public Optional<SubCriteria> findById(Long id) {
        return subCriteriaRepository.findById(id);
    }

    public Optional<SubCriteria> update(Long id, SubCriteria updatedSubCriteria) {
        Optional<SubCriteria> existingSubCriteria = subCriteriaRepository.findById(id);

        if (existingSubCriteria.isPresent()) {
            SubCriteria subCriteriaToUpdate = existingSubCriteria.get();

            subCriteriaToUpdate.setCode(updatedSubCriteria.getCode());
            subCriteriaToUpdate.setName(updatedSubCriteria.getName());

            // 3. Simpan perubahan (Karena ID ada, JPA melakukan UPDATE)
            SubCriteria savedEntity = subCriteriaRepository.save(subCriteriaToUpdate);
            return Optional.of(savedEntity);
        } else {
            return Optional.empty(); // Data tidak ditemukan
        }
    }

    public SubCriteria save(SubCriteria subCriteria) {
        return subCriteriaRepository.save(subCriteria);
    }

    public void deleteById(Long id) {
        subCriteriaRepository.deleteById(id);
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<SubCriteria> subCriterias = findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data Sub Kriteria & Crips");

        // Style Header
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Header Kolom
        String[] columns = {"No", "Kriteria", "Kode Sub", "Sub Kriteria", "Bobot Sub", "Deskripsi Crips", "Nilai Crips"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        int no = 1;
        for (SubCriteria sub : subCriterias) {
            // Jika Sub Kriteria punya Crips, tampilkan semua. Jika tidak, tampilkan baris kosong di kolom Crips.
            if (sub.getCripsList() != null && !sub.getCripsList().isEmpty()) {
                for (Crips crips : sub.getCripsList()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(no);
                    row.createCell(1).setCellValue(sub.getCriteria().getName());
                    row.createCell(2).setCellValue(sub.getCode());
                    row.createCell(3).setCellValue(sub.getName());
                    row.createCell(4).setCellValue(sub.getBobot() != null ? sub.getBobot() : 0.0);
                    row.createCell(5).setCellValue(crips.getDescription());
                    row.createCell(6).setCellValue(crips.getNilai());
                }
            } else {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(no);
                row.createCell(1).setCellValue(sub.getCriteria().getName());
                row.createCell(2).setCellValue(sub.getCode());
                row.createCell(3).setCellValue(sub.getName());
                row.createCell(4).setCellValue(sub.getBobot() != null ? sub.getBobot() : 0.0);
                row.createCell(5).setCellValue("-");
                row.createCell(6).setCellValue(0);
            }
            no++;
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
