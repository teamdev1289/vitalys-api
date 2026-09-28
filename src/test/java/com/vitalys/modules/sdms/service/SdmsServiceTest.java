package com.vitalys.modules.sdms.service;

import com.vitalys.modules.equipment.repository.InstrumentRepository;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sdms.dto.ChromatogramDataResponse;
import com.vitalys.modules.sdms.dto.DataFileLinkRequest;
import com.vitalys.modules.sdms.dto.DataFileResponse;
import com.vitalys.modules.sdms.entity.ChromatogramPeak;
import com.vitalys.modules.sdms.entity.DataFile;
import com.vitalys.modules.sdms.repository.ChromatogramPeakRepository;
import com.vitalys.modules.sdms.repository.DataFileMetadataRepository;
import com.vitalys.modules.sdms.repository.DataFileRepository;
import com.vitalys.modules.testing.repository.TestEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SdmsServiceTest {

    @Mock
    private DataFileRepository dataFileRepository;
    @Mock
    private DataFileMetadataRepository metadataRepository;
    @Mock
    private ChromatogramPeakRepository peakRepository;
    @Mock
    private InstrumentRepository instrumentRepository;
    @Mock
    private SampleRepository sampleRepository;
    @Mock
    private TestEntityRepository testEntityRepository;

    @Spy
    private ChromatogramParserService parserService = new ChromatogramParserService();

    @InjectMocks
    private SdmsService sdmsService;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "analyst_qc", "Password@123", List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Should successfully upload raw HPLC data file and calculate 21 CFR Part 11 SHA-256 checksum")
    void testUploadFileCalculatesSha256Checksum() {
        byte[] content = "Time,Intensity\n0.1,1.2\n0.2,1.3\n".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "file", "Paracetamol_Batch01.csv", "text/csv", content
        );

        when(dataFileRepository.save(any(DataFile.class))).thenAnswer(invocation -> {
            DataFile df = invocation.getArgument(0);
            df.setId(100L);
            return df;
        });

        DataFileResponse res = sdmsService.uploadFile(
                file, "CHROMATOGRAM_HPLC", 1L, 4L, 1L, 1L, "Test run"
        );

        assertThat(res).isNotNull();
        assertThat(res.getFileCode()).startsWith("SDMS-");
        assertThat(res.getChecksumSha256()).isNotNull().hasSize(64); // 64-char hex SHA-256
        assertThat(res.getOriginalFilename()).isEqualTo("Paracetamol_Batch01.csv");
        assertThat(res.getFileType()).isEqualTo("CHROMATOGRAM_HPLC");

        verify(dataFileRepository, times(1)).save(any(DataFile.class));
        verify(peakRepository, times(1)).saveAll(any());
        verify(metadataRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("Should return time-series coordinates and peak integration for InSpector 2D viewer")
    void testGetChromatogramDataForViewer() {
        DataFile file = DataFile.builder()
                .id(1L)
                .fileCode("SDMS-20260901-HPLC-001")
                .originalFilename("Paracetamol_Assay.csv")
                .fileType("CHROMATOGRAM_HPLC")
                .build();

        ChromatogramPeak peak = ChromatogramPeak.builder()
                .id(10L)
                .dataFileId(1L)
                .peakNumber(1)
                .compoundName("Paracetamol")
                .retentionTime(3.42)
                .peakHeight(98450.0)
                .peakArea(1542380.0)
                .areaPercent(99.88)
                .build();

        when(dataFileRepository.findById(1L)).thenReturn(Optional.of(file));
        when(peakRepository.findByDataFileIdOrderByPeakNumberAsc(1L)).thenReturn(List.of(peak));

        ChromatogramDataResponse chartData = sdmsService.getChromatogramData(1L);

        assertThat(chartData).isNotNull();
        assertThat(chartData.getFileCode()).isEqualTo("SDMS-20260901-HPLC-001");
        assertThat(chartData.getDatapoints()).isNotEmpty();
        assertThat(chartData.getPeaks()).hasSize(1);
        assertThat(chartData.getPeaks().get(0).getCompoundName()).isEqualTo("Paracetamol");
        assertThat(chartData.getSignalUnit()).isEqualTo("mAU");
    }

    @Test
    @DisplayName("Should link scientific data file to test and sample")
    void testLinkFile() {
        DataFile file = DataFile.builder()
                .id(2L)
                .fileCode("SDMS-20260901-UV-001")
                .fileType("SPECTRUM_UV_VIS")
                .build();

        when(dataFileRepository.findById(2L)).thenReturn(Optional.of(file));
        when(dataFileRepository.save(any(DataFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DataFileLinkRequest req = DataFileLinkRequest.builder()
                .sampleId(4L)
                .testId(2L)
                .instrumentId(2L)
                .build();

        DataFileResponse res = sdmsService.linkFile(2L, req);

        assertThat(res).isNotNull();
        assertThat(file.getSampleId()).isEqualTo(4L);
        assertThat(file.getTestId()).isEqualTo(2L);
        assertThat(file.getInstrumentId()).isEqualTo(2L);
    }
}
