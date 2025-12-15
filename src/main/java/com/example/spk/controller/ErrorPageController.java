package com.example.spk.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorPageController implements ErrorController {

    private static final String ERROR_PATH = "/error";

    @RequestMapping(ERROR_PATH)
    public String handleError(HttpServletRequest request, Model model) {

        // Dapatkan status code error dari request
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = 500; // Default ke Internal Server Error

        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
        }

        String errorMessage;
        String errorDescription;

        // Tentukan pesan berdasarkan status code
        if (statusCode == HttpStatus.NOT_FOUND.value()) { // 404
            errorMessage = "404 - Halaman Tidak Ditemukan";
            errorDescription = "Maaf, halaman yang Anda cari tidak ada. Mungkin telah dihapus, namanya diganti, atau Anda salah mengetik alamat.";
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) { // 403
            errorMessage = "403 - Akses Ditolak";
            errorDescription = "Anda tidak memiliki izin untuk mengakses sumber daya ini.";
        } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) { // 500
            errorMessage = "500 - Kesalahan Server Internal";
            errorDescription = "Terjadi kesalahan pada server saat memproses permintaan Anda. Silakan coba lagi nanti.";
        } else {
            errorMessage = "Terjadi Kesalahan Tak Terduga";
            errorDescription = "Hubungi administrator sistem jika masalah ini terus berlanjut.";
        }

        // Kirim data error ke Thymeleaf
        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("errorDescription", errorDescription);

        // Arahkan ke template error kustom
        return "error/error-custom"; // Akan mencari templates/error/error-custom.html
    }
}