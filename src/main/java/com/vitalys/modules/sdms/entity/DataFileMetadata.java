package com.vitalys.modules.sdms.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Key-value metadata extracted from scientific instrument headers (e.g. Column, Flow rate, Detector).
 */
@Entity
@Table(name = "sdms_data_file_metadata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataFileMetadata extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_file_id", nullable = false)
    private Long dataFileId;

    @Column(name = "meta_key", nullable = false, length = 100)
    private String metaKey;

    @Column(name = "meta_value", columnDefinition = "TEXT")
    private String metaValue;
}
