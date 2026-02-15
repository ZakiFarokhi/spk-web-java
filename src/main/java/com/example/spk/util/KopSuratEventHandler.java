package com.example.spk.util;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.kernel.colors.ColorConstants;

import java.io.IOException;

public class KopSuratEventHandler implements IEventHandler {
    private String logoPath;

    public KopSuratEventHandler(String logoPath) {
        this.logoPath = logoPath;
    }

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        Rectangle pageSize = page.getPageSize();
        int pageNumber = pdfDoc.getPageNumber(page);
        PdfFont times;

        try {
            times = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Hitung lebar area kerja (Lebar kertas - margin kiri & kanan)
        float areaWidth = pageSize.getWidth() - 72; // 36 kiri + 36 kanan

        PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
        Canvas canvas = new Canvas(pdfCanvas, pageSize);

        // 1. Membuat Tabel Utama dengan lebar ADAPTIF
        Table mainTable = new Table(UnitValue.createPercentArray(new float[]{1.2f, 8.8f})).setWidth(areaWidth);

        // --- Kolom Kiri: Logo ---
        try {
            Image logo = new Image(ImageDataFactory.create(logoPath)).setWidth(75);
            mainTable.addCell(new Cell().add(logo)
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE));
        } catch (Exception e) {
            mainTable.addCell(new Cell().add(new Paragraph("[LOGO]")).setBorder(Border.NO_BORDER));
        }

        // --- Kolom Kanan: Seluruh Teks Alamat ---
        Cell textCell = new Cell()
                .add(new Paragraph("PEMERINTAH PROVINSI DAERAH KHUSUS IBUKOTA JAKARTA")
                        .setFontSize(11).setTextAlignment(TextAlignment.CENTER).setMarginBottom(0))
                .add(new Paragraph("INSPEKTORAT")
                        .setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER).setMarginTop(0).setMarginBottom(0))
                .add(new Paragraph("Jalan Medan Merdeka Selatan Nomor 8-9 Telp.3822263 Fax.3813523")
                        .setFontSize(9).setTextAlignment(TextAlignment.CENTER).setMarginTop(0).setMarginBottom(0))
                .add(new Paragraph("Website: http://inspektorat.jakarta.go.id e-mail: inspektorat@jakarta.go.id")
                        .setFontSize(9).setTextAlignment(TextAlignment.CENTER).setMarginTop(0).setMarginBottom(0))
                .add(new Paragraph("JAKARTA")
                        .setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginTop(0).setMarginBottom(0))
                .add(new Paragraph("Kode Pos: 10110")
                        .setFontSize(9).setTextAlignment(TextAlignment.RIGHT).setMarginTop(2)).setBorder(Border.NO_BORDER);
        mainTable.addCell(textCell);

        // 2. Garis Bawah Adaptif
        mainTable.setBorderBottom(new SolidBorder(com.itextpdf.kernel.colors.ColorConstants.BLACK, 2f));

        // 3. Posisi Tetap di Atas (Menggunakan areaWidth agar selalu center)
        float kopBottomY = pageSize.getTop() - 120;
        mainTable.setFixedPosition(36, kopBottomY, areaWidth);
        mainTable.setFixedPosition(36, pageSize.getTop() - 120, areaWidth);

        canvas.add(mainTable);
        canvas.showTextAligned(new Paragraph("Halaman " + pageNumber).setFontSize(8).setFont(times),
                pageSize.getRight() - 36,
                kopBottomY - 15,
                TextAlignment.RIGHT);
        canvas.close();
    }
}