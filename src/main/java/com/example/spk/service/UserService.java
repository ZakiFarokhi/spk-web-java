package com.example.spk.service;

import com.example.spk.dto.UserDto;
import com.example.spk.entity.User;
import com.example.spk.repository.UserRepository;
import com.example.spk.util.KopSuratEventHandler;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.layout.element.Image;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


// --- IMPORT UNTUK PDF (iText 7) ---
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import java.io.IOException;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() { return userRepository.findAll(); }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User save(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public User create(UserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setEnabled(true);
        user.setRole(dto.getRole());
        return save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public User update(Long id, UserDto dto) {
        User user = findById(id);
        if (user == null) return null;

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setRole(dto.getRole());
        return save(user);
    }

    public void exportToPdf(HttpServletResponse response) throws Exception {
        List<User> users = userRepository.findAll();

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);

        // 1. DAFTARKAN EVENT HANDLER (KOP SURAT OTOMATIS TIAP HALAMAN)
        String logoPath = "src/main/resources/static/assets/img/logo-jakarta-bw.png";
        pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new KopSuratEventHandler(logoPath));

        // 2. SET MARGIN DOKUMEN (Atas: 130 agar tidak menimpa Kop)
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(140, 36, 40, 36);

        // 3. JUDUL LAPORAN
        document.add(new Paragraph("DAFTAR PENGGUNA SISTEM (USERS)")
                .setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));

        // 4. TABEL DATA
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 4, 4, 3})).useAllAvailableWidth();

        // Header Tabel
        table.addHeaderCell(new Cell().add(new Paragraph("No").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Username").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Email").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Role").setBold()));

        // Isi Tabel
        int no = 1;
        for (User user : users) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(no++))));
            table.addCell(new Cell().add(new Paragraph(user.getUsername())));
            table.addCell(new Cell().add(new Paragraph(user.getEmail())));
            table.addCell(new Cell().add(new Paragraph(user.getRole() != null ? user.getRole().getName() : "-")));
        }
        document.add(table);

        // 5. FOOTER TANDA TANGAN (Halaman Terakhir)
        document.add(new Paragraph("\n"));
        String tanggalStr = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID")));

        Table footerTable = new Table(1).setWidth(250f).setHorizontalAlignment(HorizontalAlignment.RIGHT);

        footerTable.addCell(new Cell().add(new Paragraph("Jakarta, " + tanggalStr)
                .setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));

        footerTable.addCell(new Cell().add(new Paragraph("Inspektur Provinsi DKI Jakarta")
                .setBold().setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));

        footerTable.addCell(new Cell().add(new Paragraph("\n\n\n")).setBorder(Border.NO_BORDER));

        footerTable.addCell(new Cell().add(new Paragraph("Dhany Sukma")
                .setBold().setUnderline().setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));

        footerTable.addCell(new Cell().add(new Paragraph("Pembina Utama Muda (IV/D)")
                .setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));

        document.add(footerTable);
        document.close();
    }

    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<User> users = findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data Users");

        // Membuat Style untuk Header
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

        // Membuat Header Row
        Row headerRow = sheet.createRow(0);
        String[] columns = {"No", "Username", "Email", "Role"};
        for (int i = 0; i < columns.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Mengisi Data
        int rowIdx = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(rowIdx - 1);
            row.createCell(1).setCellValue(user.getUsername());
            row.createCell(2).setCellValue(user.getEmail());
            row.createCell(3).setCellValue(user.getRole() != null ? user.getRole().getName() : "-");
        }

        // Auto-size kolom agar rapi
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Menulis ke output stream
        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
