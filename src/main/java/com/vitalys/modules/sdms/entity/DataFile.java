package com.vitalys.modules.sdms.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * SDMS Scientific Data File catalog entry.
 * Tracks raw analytical data files ingested from laboratory instruments (HPLC, GC, UV-Vis, FTIR)
 * with mandatory 21 CFR Part 11 SHA-256 tamper-evident integrity checksums.
 */
@Entity
@Table(name = "sdms_data_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_code", unique = true, nullable = false, length = 100)
    private String fileCode; // e.g. SDMS-20260901-HPLC-001

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType; // CHROMATOGRAM_HPLC, CHROMATOGRAM_GC, SPECTRUM_UV_VIS, SPECTRUM_FTIR, RAW_EXPORT

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "CAPTURED"; // CAPTURED, INDEXED, ARCHIVED, LOCKED

    @Column(name = "instrument_id")
    private Long instrumentId;

    @Column(name = "run_id")
    private Long runId;

    @Column(name = "sample_id")
    private Long sampleId;

    @Column(name = "test_id")
    private Long testId;

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
