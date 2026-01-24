package com.example.spk.service;

import com.example.spk.entity.Crips;
import com.example.spk.entity.Criteria;
import com.example.spk.entity.SubCriteria;
import com.example.spk.repository.SubCriteriaRepository;
import com.example.spk.util.KopSuratEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.element.Paragraph;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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
    private void checkSubWeightLimit(Long currentSubId, Long criteriaId, Double newWeight) {
        // Ambil semua sub-kriteria yang memiliki kriteria induk yang sama
        List<SubCriteria> existingSubs = subCriteriaRepository.findByCriteriaId(criteriaId);

        double totalLain = existingSubs.stream()
                .filter(s -> currentSubId == null || !s.getId().equals(currentSubId))
                .mapToDouble(s -> s.getBobot() != null ? s.getBobot() : 0.0)
                .sum();

        if ((totalLain + newWeight) > 1.0001) {
            throw new RuntimeException("Total bobot Sub-Kriteria pada kriteria ini melebihi 1.0! ");
        }
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
        checkSubWeightLimit(id,updatedSubCriteria.getCriteria().getId(), updatedSubCriteria.getBobot());
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
        checkSubWeightLimit(null,subCriteria.getCriteria().getId(), subCriteria.getBobot());

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
        headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Header Kolom
        String[] columns = {"No", "Kriteria", "Kode Sub", "Sub Kriteria", "Bobot Sub", "Deskripsi Crips", "Nilai Crips"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
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

    public void exportToPdf(HttpServletResponse response) throws Exception {
        List<SubCriteria> subList = findAll();

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);

        String logoPath = "src/main/resources/static/assets/img/logo-jakarta-bw.png";
        pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new KopSuratEventHandler(logoPath));

        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(140, 36, 40, 36);

        document.add(new Paragraph("DAFTAR SUB-KRITERIA PENILAIAN")
                .setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

        // Kolom: No, Kriteria Utama, Nama Sub-Kriteria, Bobot/Nilai
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 4, 4, 2})).useAllAvailableWidth();

        table.addHeaderCell(new Cell().add(new Paragraph("No").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Kriteria Utama").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Sub-Kriteria").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Nilai/Bobot").setBold()));

        int no = 1;
        for (SubCriteria sub : subList) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(no++))));
            // Mengambil nama kriteria dari relasi
            table.addCell(new Cell().add(new Paragraph(sub.getCriteria() != null ? sub.getCriteria().getName() : "-")));
            table.addCell(new Cell().add(new Paragraph(sub.getName())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(sub.getBobot()))));
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
