package com.example.spk.service;

import com.example.spk.dto.AuditorDto;
import com.example.spk.dto.UserDto;
import com.example.spk.entity.Auditor;
import com.example.spk.entity.SubCriteria;
import com.example.spk.entity.User;
import com.example.spk.repository.AuditorRepository;
import com.example.spk.repository.AuditorScoreRepository;
import com.example.spk.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.spk.service.AuditorService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class AuditorService {
    private final AuditorRepository auditorRepository;
    private final AuditorScoreRepository auditorScoreRepository;

    public AuditorService(AuditorRepository auditorRepository, AuditorScoreRepository auditorScoreRepository) {
        this.auditorRepository = auditorRepository;
        this.auditorScoreRepository = auditorScoreRepository;
    }

    public List<Auditor> findAll() { return auditorRepository.findAll(); }

    public Optional<Auditor> findById(Long id) {
        return auditorRepository.findById(id);
    }

    public boolean namaExists(String nama) {
        return auditorRepository.existsByName(nama);
    }

    public boolean jabatanExists(String jabatan) {
        return auditorRepository.existsByJabatan(jabatan);
    }

    public Auditor save(Auditor auditor) {
        return auditorRepository.save(auditor);
    }

    public Auditor create(AuditorDto dto) {
        Auditor auditor = new Auditor();
        auditor.setName(dto.getName());
        auditor.setNip(dto.getNip());
        auditor.setJabatan(dto.getJabatan());
        auditor.setUnit_kerja(dto.getUnit_kerja());
        auditor.setPendidikan(dto.getPendidikan());
        auditor.setEnabled(true);

        return save(auditor);
    }

    public void deleteById(Long id) {
        auditorScoreRepository.deleteByAuditor_Id(id);
        auditorRepository.deleteById(id);

    }

    public Auditor update(Long id, AuditorDto dto) {
        Optional<Auditor> a = auditorRepository.findById(id);
        Auditor auditor = a.get();
        if (auditor == null) return null;

        auditor.setName(dto.getName());
        auditor.setJabatan(dto.getJabatan());
        auditor.setUnit_kerja(dto.getUnit_kerja());
        auditor.setPendidikan(dto.getPendidikan());
        return save(auditor);
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<Auditor> auditors = findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data Auditors");

        // Style Header
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        // Membuat Header
        Row headerRow = sheet.createRow(0);
        String[] columns = {"No", "Nama", "NIP", "Jabatan", "Unit Kerja", "Pendidikan"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Mengisi Data
        int rowIdx = 1;
        for (Auditor auditor : auditors) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(rowIdx - 1);
            row.createCell(1).setCellValue(auditor.getName());
            row.createCell(2).setCellValue(auditor.getNip());
            row.createCell(3).setCellValue(auditor.getJabatan());
            row.createCell(4).setCellValue(auditor.getUnit_kerja());
            row.createCell(5).setCellValue(auditor.getPendidikan() != null ? auditor.getPendidikan().getName() : "-");
        }

        // Auto-size kolom
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
