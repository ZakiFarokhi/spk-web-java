INSERT INTO sub_criteria (code, criteria_id, name, bobot) VALUES
-- C1 PENDIDIKAN
('C1.1', 1, 'Pendidikan Formal', 0.4),
('C1.2', 1, 'Pendidikan Kauditoran (CIA,CPA,QIA,CISA)', 0.6),

-- C2 HASIL KERJA
('C2.1', 2, 'ST', 0.150),
('C2.2', 2, 'Jumlah LHP Terbit Tepat', 0.200),
('C2.3', 2, 'Kesesuaian LHP dengan Standar Audit',0.350),
('C2.4', 2, 'Kertas Kerja Pengawasan (SKP)', 0.300),

-- C3 PERILAKU KERJA
('C3.1', 3, 'LHP Revisi/Koreksi', 0.200),
('C3.2', 3, 'Jumlah LHP Belum Terbit', 0.180),
('C3.3', 3, 'Tidak Masuk Kerja Tanpa Keterangan', 0.170),
('C3.4', 3, 'Terlambat Jam Kerja', 0.160),
('C3.5', 3, 'Pulang Kerja Lebih Awal', 0.150),
('C3.6', 3, 'Hukuman Disiplin', 0.140),

-- C4 KOMPETENSI
('C4.1', 4, 'Diklat/Sertifikasi', 0.200),
('C4.2', 4, 'Ketua Tim/Kasubtim', 0.180),
('C4.3', 4, 'Pelayanan Konsultasi', 0.170),
('C4.4', 4, 'Penguasan Teknik Audit', 0.1600),
('C4.5', 4, 'Penugasan Khusus/Koordinator', 0.150),
('C4.6', 4, 'Kerjasama Tim', 0.140),

-- C5 SKP
('C5.1', 5, 'Kinerja Utama', 0.65),
('C5.2', 5, 'Pengawasan', 0.35),

-- C6 ANGKA KREDIT
('C6.1', 6, 'Pengawasan', 0.3),
('C6.2', 6, 'Pengembangan Profesi', 0.35),
('C6.3', 6, 'Pengembangan Diri', 0.35),

-- C7 PENILAIAN ATASAN
('C7.1', 7, 'Kompetensi', 0.35),
('C7.2', 7, 'Perilaku Kerja', 0.3),
('C7.3', 7, 'Kualitas Kerja', 0.35);
