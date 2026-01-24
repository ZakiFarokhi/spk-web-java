package com.example.spk.service;

import com.example.spk.entity.*;
import com.example.spk.repository.AuditorScoreRepository;
import com.example.spk.util.KopSuratEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

@Service
public class AuditorScoreService {

    // --- DEPENDENCIES ---
    @Autowired private AuditorScoreRepository auditorScoreRepository;
    @Autowired private AuditorService auditorService;
    @Autowired private CriteriaService criteriaService;
    @Autowired private SubCriteriaService subCriteriaService;
    @Autowired private CripsService cripsService;


    // --- CONSTANTS ---
    private static final Double DEFAULT_RAW_VALUE = 0.0;
    private static final Pattern RAW_VALUE_PATTERN = Pattern.compile("^rawValues\\[(\\d+)\\]$");


    // ====================================================================
    // 1. DATA RETRIEVAL & GENERATION
    // ====================================================================

    public void deleteByAuditorId(Long auditorId) {
        auditorScoreRepository.deleteByAuditor_Id(auditorId);
    }

    public List<AuditorScore> findAll(){
        return auditorScoreRepository.findAll();
    }

    public boolean isDataGenerated(Long criteriaId) {
        return auditorScoreRepository.countByCriteria_Id(criteriaId) > 0;
    }

    public List<AuditorScore> getScoresByCriteria(Long criteriaId) {
        return auditorScoreRepository.findByCriteria_Id(criteriaId);
    }

    @Transactional
    public Map<String, AuditorScore> convertListToMap(List<AuditorScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, AuditorScore> scoreMap = new HashMap<>();
        for (AuditorScore score : scores) {
            String key = score.getAuditor().getId() + "_" + score.getSubCriteria().getId();
            scoreMap.put(key, score);
        }
        return scoreMap;
    }

    public void generateDefaultScoreByAuditorId( Long auditorId){
        List<SubCriteria> subCriteriaList = subCriteriaService.findAll();
        Optional<Auditor> auditor = auditorService.findById(auditorId);

        for (SubCriteria subCriteria : subCriteriaList) {
            AuditorScore score = new AuditorScore();
            score.setAuditor(auditor.get());
            score.setCriteria(subCriteria.getCriteria());
            score.setSubCriteria(subCriteria);
            score.setCrips(null);
            score.setRawValue(DEFAULT_RAW_VALUE);
            score.setNormalizedValue(null);

            auditorScoreRepository.save(score);
        }
    }

    @Transactional
    public void generateDefaultScores(Long criteriaId) {
        if (isDataGenerated(criteriaId)) {
            return;
        }

        List<Auditor> auditors = auditorService.findAll();
        List<SubCriteria> subCriteriaList = subCriteriaService.findByCriteriaId(criteriaId);
        Optional<Criteria> criteria = criteriaService.findById(criteriaId);

        if (!criteria.isPresent()) {
            throw new RuntimeException("Kriteria dengan ID " + criteriaId + " tidak ditemukan.");
        }

        for (Auditor auditor : auditors) {
            for (SubCriteria subCriteria : subCriteriaList) {
                AuditorScore score = new AuditorScore();

                score.setAuditor(auditor);
                score.setCriteria(criteria.get());
                score.setSubCriteria(subCriteria);
                score.setCrips(null);
                score.setRawValue(DEFAULT_RAW_VALUE);
                score.setNormalizedValue(null);

                auditorScoreRepository.save(score);
            }
        }
    }


    // ====================================================================
    // 2. DATA UPDATE & PICU NORMALISASI
    // ====================================================================

    /**
     * Memproses semua parameter dari form update per baris,
     * memfilter CSRF, dan menyimpan nilai mentah yang baru.
     */
//    @Transactional
//    public void updateScoresFromMap(Map<String, String> allParams) {
//        Long lastCriteriaId = null;
//
//        for (Map.Entry<String, String> entry : allParams.entrySet()) {
//            String key = entry.getKey();
//            String rawValueStr = entry.getValue();
//
//            Matcher matcher = RAW_VALUE_PATTERN.matcher(key);
//
//            if (matcher.matches()) {
//                Long scoreId = Long.parseLong(matcher.group(1));
//                Double newRawValue;
//
//                try {
//                    newRawValue = Double.parseDouble(rawValueStr.replace(',', '.'));
//                } catch (NumberFormatException e) {
//                    throw new RuntimeException("Nilai tidak valid untuk skor ID " + scoreId + ": " + rawValueStr, e);
//                }
//
//                AuditorScore score = auditorScoreRepository.findById(scoreId)
//                        .orElseThrow(() -> new RuntimeException("Skor ID " + scoreId + " tidak ditemukan."));
//
//                // 1. Update Raw Value
//                score.setRawValue(newRawValue);
//                // 2. Clear Normalized Value (siap dihitung ulang)
//                score.setNormalizedValue(null);
//                // 3. Simpan ID Kriteria untuk pemicu normalisasi
//                lastCriteriaId = score.getCriteria().getId();
//
//                auditorScoreRepository.save(score);
//            }
//        }
//
//        // Pemicu Perhitungan Normalisasi setelah semua update selesai
//        if (lastCriteriaId != null) {
//            this.recalculateNormalization(lastCriteriaId);
//        }
//    }


    // ====================================================================
    // 3. LOGIKA NORMALISASI
    // ====================================================================

    /**
     * Menghitung dan menyimpan nilai normalisasi untuk kriteria berjenis COST (Rumus: r = min / x).
     *
     */
    // AuditorScoreService.java

    /**
     * Menghitung dan menyimpan nilai normalisasi COST untuk setiap Sub-Kriteria di bawah criteriaId.
     * Rumus: r = min / x
     */
    private void calculateNormalizationCost(Long criteriaId) {
        // 1. Ambil semua Sub-Kriteria di bawah Kriteria C3
        List<SubCriteria> subCriteriaList = subCriteriaService.findByCriteriaId(criteriaId);

        if (subCriteriaList.isEmpty()) {
            System.out.println("Tidak ada Sub-Kriteria ditemukan untuk Kriteria ID: " + criteriaId);
            return;
        }

        // 2. Lakukan Normalisasi untuk SETIAP SUB-KRITERIA
        for (SubCriteria subCriteria : subCriteriaList) {
            Long subCriteriaId = subCriteria.getId();

            // Ambil semua skor auditor HANYA untuk Sub-Kriteria ini
            // ASUMSI: Anda memiliki method findBySubCriteria_Id di Repository
            List<AuditorScore> scoresForSubCriteria = auditorScoreRepository.findBySubCriteria_Id(subCriteriaId);

            if (scoresForSubCriteria.isEmpty()) continue;

            // 3. Hitung Nilai Minimum (min_i {x_ij}) untuk Sub-Kriteria ini
            double minValue = scoresForSubCriteria.stream()
                    .mapToDouble(AuditorScore::getRawValue)
                    .filter(v -> v > 0)
                    .min()
                    .orElse(1.0); // Default ke 1.0 jika semua data 0

            // 4. Iterasi dan Terapkan Rumus Cost PADA SKOR SUB-KRITERIA INI
            for (AuditorScore score : scoresForSubCriteria) {
                double rawValue = score.getRawValue();
                double normalizedValue;

                if (rawValue <= 0.0) {
                    normalizedValue = 0.0;
                } else {
                    normalizedValue = minValue / rawValue;
                }

                score.setNormalizedValue(normalizedValue);
                auditorScoreRepository.save(score);
            }
            System.out.println("Normalisasi Cost selesai untuk Sub-Kriteria: " + subCriteria.getCode());
        }
    }

    /**
     * Metode pembantu: Menyalin Raw Value ke Normalized Value.
     * Digunakan untuk kriteria BENEFIT.
     */
    @Transactional
    private void setRawValueAsNormalized(Long criteriaId) {
        List<AuditorScore> scores = auditorScoreRepository.findByCriteria_Id(criteriaId);
        for (AuditorScore score : scores) {
            System.out.println("Loaded 1");
            // Logika: Jika BENEFIT, normalizedValue = rawValue
            score.setNormalizedValue(score.getRawValue());
            auditorScoreRepository.save(score);
        }
    }

    // ====================================================================
    // 4. PICU NORMALISASI ULANG (Fleksibel)
    // ====================================================================

    /**
     * Pemicu utama untuk menghitung ulang normalisasi.
     * Memanggil perhitungan berdasarkan nilai 'indeks' (COST/BENEFIT) dari Entitas Criteria.
     */
    @Transactional
    public void recalculateNormalization(Long criteriaId) {
        Criteria criteria = criteriaService.findById(criteriaId)
                .orElseThrow(() -> new RuntimeException("Kriteria ID " + criteriaId + " tidak ditemukan untuk normalisasi."));

        String criteriaIndeks = criteria.getIndeks();
        System.out.println("INDEKS -"+criteriaId+" - "+ criteriaIndeks);

        if ("COST".equalsIgnoreCase(criteriaIndeks)) {
            calculateNormalizationCost(criteriaId);
            System.out.println("Normalisasi Cost selesai untuk kriteria: " + criteria.getCode() + ".");

        } else if ("BENEFIT".equalsIgnoreCase(criteriaIndeks)) {
            // Logika: Jika BENEFIT, rawValue disalin sebagai normalizedValue.
            setRawValueAsNormalized(criteriaId);
            System.out.println("Normalisasi Benefit selesai: Raw Value disalin untuk kriteria: " + criteria.getCode() + ".");

        } else {
            System.out.println("Tipe indeks kriteria tidak dikenal: " + criteriaIndeks);
        }
    }

    @Transactional
    public void updateScoresByCripsId(Map<Long, Long> scoresToUpdate) {

        // Inisialisasi variabel untuk melacak Kriteria yang diubah
        Long lastCriteriaId = null;

        for (Map.Entry<Long, Long> entry : scoresToUpdate.entrySet()) {
            Long scoreId = entry.getKey();
            Long newCripsId = entry.getValue();

            // 1. Ambil AlternativeScore yang akan diupdate
            AuditorScore score = auditorScoreRepository.findById(scoreId)
                    .orElseThrow(() -> new IllegalArgumentException("AlternativeScore tidak ditemukan dengan ID: " + scoreId));

            // 2. Ambil objek Crips yang baru berdasarkan ID yang dipilih
            Crips newCrips = cripsService.findById(newCripsId)
                    .orElseThrow(() -> new IllegalArgumentException("Crips tidak ditemukan dengan ID: " + newCripsId));

            // 3. Update relasi Crips
            score.setCrips(newCrips);

            // 4. Update rawValue (PENTING)
            score.setRawValue(newCrips.getNilai());

            // 5. Kosongkan Normalized Value (menandakan perlu dihitung ulang)
            score.setNormalizedValue(null);

            // 6. Simpan ID Kriteria sebelum menyimpan skor
            if (score.getCriteria() != null) {
                lastCriteriaId = score.getCriteria().getId();
            }

            // 7. Simpan perubahan
            auditorScoreRepository.save(score);
        }

        // Pemicu Perhitungan Normalisasi setelah semua update selesai
        if (lastCriteriaId != null) {
            System.out.println("NORMALIZATION TRIGGERED by Crips Update for Criteria ID: " + lastCriteriaId);
            this.recalculateNormalization(lastCriteriaId);
        }
    }

    public void exportToExcel(Long criteriaId, HttpServletResponse response) throws IOException {
        Criteria criteria = criteriaService.findById(criteriaId)
                .orElseThrow(() -> new RuntimeException("Kriteria tidak ditemukan"));

        List<Auditor> auditors = auditorService.findAll();
        List<SubCriteria> subCriteriaList = subCriteriaService.findByCriteriaId(criteriaId);
        List<AuditorScore> scores = getScoresByCriteria(criteriaId);
        Map<String, AuditorScore> scoreMap = convertListToMap(scores);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Konversi - " + criteria.getName());

        // --- STYLE ---
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        // --- HEADER ---
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("No");
        headerRow.createCell(1).setCellValue("Nama Auditor");

        // Header dinamis berdasarkan Sub Kriteria
        int colIdx = 2;
        for (SubCriteria sc : subCriteriaList) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(colIdx++);
            cell.setCellValue(sc.getCode() + " - " + sc.getName());
            cell.setCellStyle(headerStyle);
        }

        // --- DATA ---
        int rowIdx = 1;
        for (int i = 0; i < auditors.size(); i++) {
            Auditor auditor = auditors.get(i);
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(auditor.getName());

            int currentCol = 2;
            for (SubCriteria sc : subCriteriaList) {
                String key = auditor.getId() + "_" + sc.getId();
                AuditorScore score = scoreMap.get(key);

                if (score != null && score.getCrips() != null) {
                    // Menampilkan Deskripsi dan Nilai dalam kurung
                    row.createCell(currentCol++).setCellValue(
                            score.getCrips().getDescription() + " (" + score.getRawValue() + ")"
                    );
                } else {
                    row.createCell(currentCol++).setCellValue("-");
                }
            }
        }

        // Auto-size kolom
        for (int j = 0; j < colIdx; j++) {
            sheet.autoSizeColumn(j);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public void exportToPdf(Long criteriaId, HttpServletResponse response) throws Exception {
        Criteria criteria = criteriaService.findById(criteriaId)
                .orElseThrow(() -> new RuntimeException("Kriteria tidak ditemukan"));

        List<Auditor> auditors = auditorService.findAll();
        List<SubCriteria> subCriteriaList = subCriteriaService.findByCriteriaId(criteriaId);
        List<AuditorScore> scores = getScoresByCriteria(criteriaId);
        Map<String, AuditorScore> scoreMap = convertListToMap(scores);

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);

        // Daftarkan Kop Surat
        String logoPath = "src/main/resources/static/assets/img/logo-jakarta-bw.png";
        pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new KopSuratEventHandler(logoPath));

        // Gunakan Landscape jika sub-kriteria lebih dari 3 agar tidak sesak
        PageSize pageSize = subCriteriaList.size() > 3 ? PageSize.A4.rotate() : PageSize.A4;
        Document document = new Document(pdf, pageSize);
        document.setMargins(140, 36, 40, 36);

        document.add(new Paragraph("LAPORAN KONVERSI NILAI AUDITOR")
                .setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));
        document.add(new Paragraph("Kriteria: " + criteria.getName() + " (" + criteria.getCode() + ")")
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

        // Hitung lebar kolom: No (1), Nama (4), Sub-Kriteria (2 per kolom)
        int totalCols = 2 + subCriteriaList.size();
        float[] relativeWidths = new float[totalCols];
        relativeWidths[0] = 1f;
        relativeWidths[1] = 4f;
        for (int i = 2; i < totalCols; i++) relativeWidths[i] = 3f;

        com.itextpdf.layout.element.Table table = new Table(UnitValue.createPercentArray(relativeWidths)).useAllAvailableWidth();

        // Header Tabel
        table.addHeaderCell(new Cell().add(new Paragraph("No").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Nama Auditor").setBold()));
        for (SubCriteria sc : subCriteriaList) {
            table.addHeaderCell(new Cell().add(new Paragraph(sc.getCode()).setBold()));
        }

        // Isi Data
        int no = 1;
        for (Auditor auditor : auditors) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(no++))));
            table.addCell(new Cell().add(new Paragraph(auditor.getName())));

            for (SubCriteria sc : subCriteriaList) {
                String key = auditor.getId() + "_" + sc.getId();
                AuditorScore score = scoreMap.get(key);
                String valTxt = (score != null && score.getCrips() != null)
                        ? score.getCrips().getDescription() + " (" + score.getRawValue() + ")"
                        : "-";
                table.addCell(new Cell().add(new Paragraph(valTxt).setFontSize(9)));
            }
        }
        document.add(table);

        // Footer Tanda Tangan
        document.add(new Paragraph("\n"));
        String tgl = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID")));
        Table footer = new Table(1).setWidth(250f).setHorizontalAlignment(HorizontalAlignment.RIGHT);
        footer.addCell(new Cell().add(new Paragraph("Jakarta, " + tgl).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
        footer.addCell(new Cell().add(new Paragraph("Inspektur Provinsi DKI Jakarta").setBold().setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
        footer.addCell(new Cell().add(new Paragraph("\n\n\n")).setBorder(Border.NO_BORDER));
        footer.addCell(new Cell().add(new Paragraph("Dhany Sukma").setBold().setUnderline().setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
        footer.addCell(new Cell().add(new Paragraph("Pembina Utama Muda (IV/D)").setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));

        document.add(footer);
        document.close();
    }
}