package com.example.spk.service;

import com.example.spk.dto.AuditorDto;
import com.example.spk.entity.Auditor;
import com.example.spk.repository.AuditorRepository;
import com.example.spk.repository.AuditorScoreRepository;
import com.example.spk.util.KopSuratEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.layout.element.Image;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.element.Paragraph;

import com.itextpdf.layout.element.Table;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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
        headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        // Membuat Header
        Row headerRow = sheet.createRow(0);
        String[] columns = {"No", "Nama", "NIP", "Jabatan", "Unit Kerja", "Pendidikan"};
        for (int i = 0; i < columns.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
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

    public void exportToPdf(HttpServletResponse response) throws Exception {
        List<Auditor> auditors = auditorRepository.findAll();

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);

        // DAFTARKAN EVENT HANDLER DI SINI
        String logoPath = "src/main/resources/static/assets/img/logo-jakarta-bw.png";
        pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new KopSuratEventHandler(logoPath));

        // Margin atas harus cukup besar (misal 130) agar isi tabel tidak menabrak Kop
        Document document = new Document(pdf, PageSize.A4);
        // Di AuditorService / UserService
        document.setMargins(130, 36, 36, 36);

        // Isi Laporan
        document.add(new Paragraph("DAFTAR PEGAWAI / AUDITOR")
                .setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 4, 3})).useAllAvailableWidth();
        table.addHeaderCell("No");
        table.addHeaderCell("NIP");
        table.addHeaderCell("Nama Lengkap");
        table.addHeaderCell("Jabatan");

        int no = 1;
        for (Auditor auditor : auditors) {
            table.addCell(String.valueOf(no++));
            table.addCell(auditor.getNip());
            table.addCell(auditor.getName());
            table.addCell(auditor.getJabatan());
        }
        document.add(table);

        // Footer Tanda Tanggan (Tetap di akhir dokumen saja)
        document.add(new Paragraph("\n"));
        String tanggalStr = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID")));

        Table footerTable = new Table(1).setWidth(250f).setHorizontalAlignment(HorizontalAlignment.RIGHT);
        footerTable.addCell(new Cell().add(new Paragraph("Jakarta, " + tanggalStr).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
        footerTable.addCell(new Cell().add(new Paragraph("Inspektur Provinsi DKI Jakarta").setBold().setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
        footerTable.addCell(new Cell().add(new Paragraph("\n\n\n")).setBorder(Border.NO_BORDER));
        footerTable.addCell(new Cell().add(new Paragraph("Dhany Sukma").setBold().setUnderline().setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
        footerTable.addCell(new Cell().add(new Paragraph("Pembina Utama Muda (IV/D)").setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));

        document.add(footerTable);
        document.close();
    }

}
