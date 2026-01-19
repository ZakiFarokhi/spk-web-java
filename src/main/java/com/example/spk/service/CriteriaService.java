package com.example.spk.service;

import com.example.spk.entity.Criteria;
import com.example.spk.entity.SubCriteria;
import com.example.spk.repository.CriteriaRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class CriteriaService {

    private final CriteriaRepository criteriaRepository;

    public CriteriaService(CriteriaRepository criteriaRepository) {
        this.criteriaRepository = criteriaRepository;
    }

    @Transactional()
    public List<Criteria> findAllWithSubCriterias() {
        return criteriaRepository.findAllWithSubCriterias();
    }
    public List<Criteria> findAll() {
        return criteriaRepository.findAll();
    }

    public Optional<Criteria> findById(Long id) {
        return criteriaRepository.findById(id);
    }

    public Criteria save(Criteria criteria) {
        return criteriaRepository.save(criteria);
    }

    public Optional<Criteria> findCriteriaByIdWithDetails(Long criteriaId) {
        // Memanggil Query Method khusus yang menggunakan JOIN FETCH
        return criteriaRepository.findByIdWithDetails(criteriaId);
    }

    public Optional<Criteria> update(Long id, Criteria updatedCriteria) {
        Optional<Criteria> existingCriteria = criteriaRepository.findById(id);

        if (existingCriteria.isPresent()) {
            Criteria criteriaToUpdate = existingCriteria.get();

            criteriaToUpdate.setCode(updatedCriteria.getCode());
            criteriaToUpdate.setName(updatedCriteria.getName());
            criteriaToUpdate.setBobot(updatedCriteria.getBobot());
            criteriaToUpdate.setIndeks(updatedCriteria.getIndeks());

            // 3. Simpan perubahan (Karena ID ada, JPA melakukan UPDATE)
            Criteria savedEntity = criteriaRepository.save(criteriaToUpdate);
            return Optional.of(savedEntity);
        } else {
            return Optional.empty(); // Data tidak ditemukan
        }
    }

    public void deleteById(Long id) {
        criteriaRepository.deleteById(id);
    }

    public boolean existsByCode(String code) {
        return criteriaRepository.existsByCode(code);
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<Criteria> criterias = findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data Kriteria");

        // Style Header
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Membuat Header
        Row headerRow = sheet.createRow(0);
        String[] columns = {"No", "Kode", "Nama Kriteria", "Bobot", "Indeks (Benefit/Cost)"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Mengisi Data
        int rowIdx = 1;
        for (Criteria criteria : criterias) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(rowIdx - 1);
            row.createCell(1).setCellValue(criteria.getCode());
            row.createCell(2).setCellValue(criteria.getName());
            row.createCell(3).setCellValue(criteria.getBobot());
            row.createCell(4).setCellValue(criteria.getIndeks());
        }

        // Auto-size kolom
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
