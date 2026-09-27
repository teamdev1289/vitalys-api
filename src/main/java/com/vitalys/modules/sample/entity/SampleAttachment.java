package com.vitalys.modules.sample.entity;

import com.vitalys.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * Attachments for sample
 */
@Entity
@Table(name = "sample_sample_attachment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SampleAttachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sample_id")
    private Long sampleId;

    @Column(name = "filename")
    private String filename;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "uploaded_by")
    private String uploadedBy;


}
