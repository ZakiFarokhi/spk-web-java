package com.example.spk.service;

import com.example.spk.entity.Criteria;
import com.example.spk.repository.CriteriaRepository;
import com.example.spk.util.KopSuratEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.HorizontalAlignment;

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
        headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Membuat Header
        Row headerRow = sheet.createRow(0);
        String[] columns = {"No", "Kode", "Nama Kriteria", "Bobot", "Indeks (Benefit/Cost)"};
        for (int i = 0; i < columns.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
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

    public void exportToPdf(HttpServletResponse response) throws Exception {
        List<Criteria> criterias = findAll();

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);

        // Daftarkan Kop Surat otomatis
        String logoPath = "src/main/resources/static/assets/img/logo-jakarta-bw.png";
        pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new KopSuratEventHandler(logoPath));

        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(135, 36, 40, 36);

        // Judul
        document.add(new Paragraph("DAFTAR KRITERIA PENILAIAN")
                .setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

        // Tabel
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 4, 2, 3})).useAllAvailableWidth();

        table.addHeaderCell(new Cell().add(new Paragraph("No").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Kode").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Nama Kriteria").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Bobot").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Sifat").setBold()));

        int no = 1;
        for (Criteria criteria : criterias) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(no++))));
            table.addCell(new Cell().add(new Paragraph(criteria.getCode())));
            table.addCell(new Cell().add(new Paragraph(criteria.getName())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(criteria.getBobot()))));
            table.addCell(new Cell().add(new Paragraph(criteria.getIndeks()))); // Benefit / Cost
        }
        document.add(table);

        // Footer Tanda Tangan
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
