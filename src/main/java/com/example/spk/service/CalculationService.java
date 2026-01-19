package com.example.spk.service;

import com.example.spk.entity.*;
import com.example.spk.repository.AuditorRepository;
import com.example.spk.repository.AuditorScoreRepository;
import com.example.spk.repository.CriteriaRepository;
import com.example.spk.util.KopSuratEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CalculationService {

    @Autowired
    private AuditorRepository auditorRepository;
    @Autowired
    private AuditorScoreRepository auditorScoreRepository;
    @Autowired
    private CriteriaRepository criteriaRepository;

    // Asumsi: Semua skor sudah dinormalisasi oleh AuditorScoreService

    /**
     * Menghitung Matriks Pembobotan (V) dan Skor Akhir (Preferensi) untuk semua Auditor.
     * Rumus SAW: V_i = Σ (W_j * r_ij)
     * * @return List hasil perhitungan peringkat
     *
     */

    @Transactional(readOnly = true)
    public Map<String, Double> calculateAggregatedNormalizationMatrix() {

        // 1. Ambil semua skor
        List<AuditorScore> allScores = auditorScoreRepository.findAll();

        // 2. Kelompokkan skor berdasarkan Auditor dan Kriteria
        Map<Auditor, Map<Criteria, List<AuditorScore>>> groupedScores = allScores.stream()
                .collect(Collectors.groupingBy(
                        AuditorScore::getAuditor,
                        Collectors.groupingBy(AuditorScore::getCriteria)
                ));

        Map<String, Double> aggregatedMap = new HashMap<>();

        // 3. Iterasi dan Aggregasi (Hitung C_j,norm menggunakan normalized_value)
        for (Map.Entry<Auditor, Map<Criteria, List<AuditorScore>>> entryAuditor : groupedScores.entrySet()) {
            Auditor auditor = entryAuditor.getKey();

            for (Map.Entry<Criteria, List<AuditorScore>> entryCriteria : entryAuditor.getValue().entrySet()) {
                // Kita tidak perlu lagi membalik C3 di sini, karena diasumsikan
                // kolom normalized_value sudah mencerminkan nilai yang benar untuk C3.

                Criteria criteria = entryCriteria.getKey();
                List<AuditorScore> subScores = entryCriteria.getValue();

                double aggregatedScore = 0.0;

                for (AuditorScore score : subScores) {
                    // PENTING: Menggunakan nilai yang sudah dinormalisasi dari database
                    Double normalizedValue = score.getNormalizedValue() != null ? score.getNormalizedValue() : 0.0;

                    // Ambil Bobot Sub-Kriteria (W_sk)
                    Double subWeight = score.getSubCriteria() != null && score.getSubCriteria().getBobot() != null
                            ? score.getSubCriteria().getBobot()
                            : 0.0;

                    // Agregasi: C_j,norm += (Normalized Value * Sub-Weight)
                    aggregatedScore += normalizedValue * subWeight;
                }

                // Simpan hasil aggregasi (Matriks C_j,norm)
                // C_j,norm sekarang berada di rentang 0-1.
                String key = auditor.getId() + "_" + criteria.getId();
                aggregatedMap.put(key, aggregatedScore);
            }
        }

        return aggregatedMap; // Map berisi skor C_j,norm (rentang 0-1) per Auditor
    }

    @Transactional(readOnly = true)
    public Map<String, Double> calculateFinalNormalizedCriteriaMatrix() {

        // 1. Ambil Matriks hasil aggregasi dari Sub-Kriteria (C_j,norm)
        Map<String, Double> aggregatedMap = this.calculateAggregatedNormalizationMatrix();

        // 2. Kelompokkan berdasarkan Kriteria (untuk mencari Max per Kriteria)
        Map<Long, List<Double>> scoresByCriteriaId = new HashMap<>();

        // Inisialisasi Kriteria ID 3 (C3)
        final Long CRITERIA_ID_C3 = 3L;

        for (Map.Entry<String, Double> entry : aggregatedMap.entrySet()) {
            String key = entry.getKey(); // Format: "AuditorId_CriteriaId"

            String[] parts = key.split("_");
            if (parts.length < 2) continue;
            Long criteriaId = Long.parseLong(parts[1]);

            scoresByCriteriaId.computeIfAbsent(criteriaId, k -> new ArrayList<>()).add(entry.getValue());
        }

        // 3. Hitung Nilai Maksimum (Max) per Kriteria
        Map<Long, Double> maxPerCriteria = scoresByCriteriaId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().mapToDouble(Double::doubleValue).max().orElse(1.0)
                ));

        // Ambil Max Global untuk C3
        Double maxC3 = maxPerCriteria.getOrDefault(CRITERIA_ID_C3, 1.0);


        // 4. Normalisasi Pembagian Maksimum (r = x / max)
        Map<String, Double> finalNormalizedMap = new HashMap<>();

        for (Map.Entry<String, Double> entry : aggregatedMap.entrySet()) {
            String key = entry.getKey();
            Double aggregatedScore = entry.getValue(); // Skor C_j,norm

            String[] parts = key.split("_");
            if (parts.length < 2) continue;
            Long criteriaId = Long.parseLong(parts[1]);

            Double maxValue = maxPerCriteria.getOrDefault(criteriaId, 1.0);

            double finalScore = (maxValue > 0) ? (aggregatedScore / maxValue) : 0.0;

            finalNormalizedMap.put(key, finalScore);
        }

        return finalNormalizedMap;
    }

    @Transactional(readOnly = true)
    public List<RankingResult> calculateFinalRanking() {

        // 1. Ambil Auditor
        List<Auditor> auditors = auditorRepository.findAll();

        // 2. Mendapatkan Matriks Keputusan Ternormalisasi Akhir (R_ij)
        //    Input dari fungsi normalisasi kriteria sebelumnya.
        Map<String, Double> finalNormalizedMatrix = this.calculateFinalNormalizedCriteriaMatrix();

        // 3. Ambil Bobot Kriteria Utama (W_j)
        List<Criteria> criteriaList = criteriaRepository.findAll();
        Map<Long, Double> criteriaWeights = criteriaList.stream()
                .collect(Collectors.toMap(
                        Criteria::getId,
                        criteria -> criteria.getBobot() != null ? criteria.getBobot() : 0.0
                ));

        // 4. Hitung Skor Preferensi Akhir (Final Score) per Auditor
        List<RankingResult> rankingResults = auditors.stream()
                .map(auditor -> {
                    double finalScore = 0.0;

                    // Iterasi melalui semua Kriteria Utama
                    for (Criteria criteria : criteriaList) {

                        String key = auditor.getId() + "_" + criteria.getId();

                        // A. Ambil Matriks Ternormalisasi (R_ij)
                        Double normalizedScore = finalNormalizedMatrix.getOrDefault(key, 0.0);

                        // B. Ambil Bobot Kriteria Utama (W_j)
                        Double weight = criteriaWeights.getOrDefault(criteria.getId(), 0.0);

                        // C. Perkalian dan Agregasi
                        double weightedContribution = normalizedScore * weight;
                        finalScore += weightedContribution;

                    }


                    return new RankingResult(auditor, finalScore);
                })
                .collect(Collectors.toList());

        // 5. Urutkan Ranking (Skor tertinggi adalah peringkat 1)
        rankingResults.sort(Comparator.comparingDouble(RankingResult::getFinalScore).reversed());

        for (int i = 0; i < rankingResults.size(); i++) {
            RankingResult result = rankingResults.get(i);
        }

        return rankingResults;
    }

    // --- HELPER UNTUK PDF ---
    private Document preparePdf(HttpServletResponse response, PageSize pageSize) throws Exception {
        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new KopSuratEventHandler("src/main/resources/static/assets/img/logo-jakarta-bw.png"));
        Document doc = new Document(pdf, pageSize);
        doc.setMargins(135, 36, 40, 36);
        return doc;
    }

    private void addFooter(Document doc) {
        doc.add(new Paragraph("\n"));
        String tgl = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID")));
        Table ft = new Table(1).setWidth(250f).setHorizontalAlignment(HorizontalAlignment.RIGHT);
        ft.addCell(new Cell().add(new Paragraph("Jakarta, " + tgl).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
        ft.addCell(new Cell().add(new Paragraph("Inspektur Provinsi DKI Jakarta").setBold().setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
        ft.addCell(new Cell().add(new Paragraph("\n\n\n")).setBorder(Border.NO_BORDER));
        ft.addCell(new Cell().add(new Paragraph("Dhany Sukma").setBold().setUnderline().setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
        ft.addCell(new Cell().add(new Paragraph("Pembina Utama Muda (IV/D)").setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
        doc.add(ft);
    }

    // ====================================================================
    // 1. PDF NORMALISASI (SUB-KRITERIA)
    // ====================================================================
    public void exportNormalizationPdf(HttpServletResponse response) throws Exception {
        List<Auditor> auditors = auditorRepository.findAll();
        List<Criteria> criteriaList = criteriaRepository.findAll();
        List<AuditorScore> allScores = auditorScoreRepository.findAll();

        Map<String, AuditorScore> scoreMap = allScores.stream()
                .collect(Collectors.toMap(s -> s.getAuditor().getId() + "_" + s.getSubCriteria().getId(), s -> s));

        List<SubCriteria> subList = criteriaList.stream()
                .flatMap(c -> c.getSubCriteriaList().stream())
                .collect(Collectors.toList());

        // Menggunakan Landscape (Mendatar)
        Document doc = preparePdf(response, PageSize.A4.rotate());

        doc.add(new Paragraph("MATRIKS NORMALISASI SUB-KRITERIA")
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(12));

        // Buat tabel dengan ukuran font lebih kecil (fontSize 7 atau 8)
        float fontSizeTable = 7f;

        // Menggunakan lebar kolom proporsional: No (kecil), Nama (sedang), Sisanya rata
        int totalCols = subList.size() + 2;
        float[] columnWidths = new float[totalCols];
        columnWidths[0] = 0.5f; // Kolom No
        columnWidths[1] = 2.5f; // Kolom Nama Auditor
        for (int i = 2; i < totalCols; i++) columnWidths[i] = 1f; // Kolom Sub-Kriteria

        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

        // Header Tabel
        table.addHeaderCell(new Cell().add(new Paragraph("No").setBold().setFontSize(fontSizeTable)));
        table.addHeaderCell(new Cell().add(new Paragraph("Nama Auditor").setBold().setFontSize(fontSizeTable)));

        for (SubCriteria sc : subList) {
            table.addHeaderCell(new Cell().add(new Paragraph(sc.getCode())
                    .setBold()
                    .setFontSize(fontSizeTable)
                    .setTextAlignment(TextAlignment.CENTER)));
        }

        // Isi Data
        int no = 1;
        for (Auditor a : auditors) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(no++))
                    .setFontSize(fontSizeTable)));
            table.addCell(new Cell().add(new Paragraph(a.getName())
                    .setFontSize(fontSizeTable)));

            for (SubCriteria sc : subList) {
                AuditorScore s = scoreMap.get(a.getId() + "_" + sc.getId());
                String val = (s != null && s.getNormalizedValue() != null)
                        ? String.format("%.3f", s.getNormalizedValue())
                        : "0.000";

                table.addCell(new Cell().add(new Paragraph(val)
                        .setFontSize(fontSizeTable)
                        .setTextAlignment(TextAlignment.CENTER)));
            }
        }

        doc.add(table);


        addFooter(doc);
        doc.close();
    }

    // ====================================================================
    // 2. PDF AGGREGATION (Cj,norm)
    // ====================================================================
    public void exportAggregatedPdf(HttpServletResponse response) throws Exception {
        Map<String, Double> data = calculateAggregatedNormalizationMatrix();
        exportMatrixPdf(response, "MATRIKS HASIL AGGREGASI (Cj,norm)", data);
    }

    // ====================================================================
    // 3. PDF FINAL NORMALIZATION (Rij)
    // ====================================================================
    public void exportFinalNormalizedPdf(HttpServletResponse response) throws Exception {
        Map<String, Double> data = calculateFinalNormalizedCriteriaMatrix();
        exportMatrixPdf(response, "MATRIKS NORMALISASI FINAL (Rij)", data);
    }

    // Helper untuk Tahap 2 & 3 karena strukturnya sama (Kriteria Utama)
    private void exportMatrixPdf(HttpServletResponse response, String title, Map<String, Double> data) throws Exception {
        List<Auditor> auditors = auditorRepository.findAll();
        List<Criteria> criteriaList = criteriaRepository.findAll();

        Document doc = preparePdf(response, PageSize.A4);
        doc.add(new Paragraph(title).setBold().setTextAlignment(TextAlignment.CENTER));

        Table table = new Table(UnitValue.createPercentArray(criteriaList.size() + 2)).useAllAvailableWidth();
        table.addHeaderCell("No"); table.addHeaderCell("Nama Auditor");
        for (Criteria c : criteriaList) table.addHeaderCell(c.getCode());

        int no = 1;
        for (Auditor a : auditors) {
            table.addCell(String.valueOf(no++)); table.addCell(a.getName());
            for (Criteria c : criteriaList) {
                Double val = data.get(a.getId() + "_" + c.getId());
                table.addCell(String.format("%.4f", val != null ? val : 0.0));
            }
        }
        doc.add(table); addFooter(doc); doc.close();
    }

    // ====================================================================
    // 4. PDF RANKING AKHIR
    // ====================================================================
    public void exportRankingPdf(HttpServletResponse response) throws Exception {
        List<RankingResult> results = calculateFinalRanking();
        Document doc = preparePdf(response, PageSize.A4);
        doc.add(new Paragraph("LAPORAN HASIL PERANGKINGAN AKHIR (SAW)").setBold().setTextAlignment(TextAlignment.CENTER));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 5, 3})).useAllAvailableWidth();
        table.addHeaderCell("Rank"); table.addHeaderCell("Nama Auditor"); table.addHeaderCell("Skor Akhir");

        int rank = 1;
        for (RankingResult res : results) {
            table.addCell(String.valueOf(rank++));
            table.addCell(res.getAuditor().getName());
            table.addCell(String.format("%.4f", res.getFinalScore()));
        }
        doc.add(table); addFooter(doc); doc.close();
    }
}