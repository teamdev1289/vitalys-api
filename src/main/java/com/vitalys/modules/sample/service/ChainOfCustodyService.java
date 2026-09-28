package com.vitalys.modules.sample.service;

import com.vitalys.modules.sample.dto.CustodyLogResponse;
import com.vitalys.modules.sample.dto.CustodyTransferRequest;
import com.vitalys.modules.sample.entity.Sample;
import com.vitalys.modules.sample.entity.SampleChainOfCustody;
import com.vitalys.modules.sample.repository.SampleChainOfCustodyRepository;
import com.vitalys.modules.sample.repository.SampleRepository;
import com.vitalys.modules.sys.annotation.Auditable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChainOfCustodyService {

    private final SampleChainOfCustodyRepository custodyRepository;
    private final SampleRepository sampleRepository;

    @Transactional(readOnly = true)
    public List<CustodyLogResponse> getCustodyHistory(Long sampleId) {
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found with ID: " + sampleId));

        return custodyRepository.findBySampleIdOrderByTransferredAtDesc(sampleId).stream()
                .map(c -> CustodyLogResponse.builder()
                        .id(c.getId())
                        .sampleId(c.getSampleId())
                        .sampleCode(sample.getSampleCode())
                        .fromUser(c.getFromUser())
                        .toUser(c.getToUser())
                        .fromLocation(c.getFromLocation())
                        .toLocation(c.getToLocation())
                        .transferredAt(c.getTransferredAt())
                        .purpose(c.getPurpose())
                        .sampleCondition(c.getSampleCondition())
                        .storageCondition(c.getStorageCondition())
                        .signatureToken(c.getSignatureToken())
                        .notes(c.getNotes())
                        .build())
                .toList();
    }

    /**
     * Transfer physical custody and location of a sample.
     * Updates current location, storage condition, and logs immutable CoC record.
     */
    @Auditable(module = "SAMPLE", entity = "SampleChainOfCustody")
    @Transactional
    public CustodyLogResponse transferCustody(Long sampleId, CustodyTransferRequest request) {
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found with ID: " + sampleId));

        String currentUser = getCurrentUsername();
        String fromLocation = sample.getCurrentLocation();
        OffsetDateTime now = OffsetDateTime.now();

        SampleChainOfCustody custody = SampleChainOfCustody.builder()
                .sampleId(sampleId)
                .fromUser(currentUser)
                .toUser(request.getToUser().trim())
                .fromLocation(fromLocation != null ? fromLocation : "Unknown")
                .toLocation(request.getToLocation().trim())
                .transferredAt(now)
                .purpose(request.getPurpose().trim())
                .sampleCondition(request.getSampleCondition())
                .storageCondition(request.getStorageCondition() != null ? request.getStorageCondition() : sample.getStorageCondition())
                .signatureToken(request.getSignatureToken())
                .notes(request.getNotes())
                .build();

        SampleChainOfCustody saved = custodyRepository.save(custody);

        // Update sample current location and condition
        sample.setCurrentLocation(request.getToLocation().trim());
        if (request.getStorageCondition() != null && !request.getStorageCondition().isBlank()) {
            sample.setStorageCondition(request.getStorageCondition().trim());
        }
        sample.setAssignedTo(request.getToUser().trim());
        sampleRepository.save(sample);

        log.info("Custody transferred for sample {} [ID: {}]: {} -> {} at location '{}' (Purpose: {})",
                sample.getSampleCode(), sampleId, currentUser, request.getToUser(), request.getToLocation(), request.getPurpose());

        return CustodyLogResponse.builder()
                .id(saved.getId())
                .sampleId(saved.getSampleId())
                .sampleCode(sample.getSampleCode())
                .fromUser(saved.getFromUser())
                .toUser(saved.getToUser())
                .fromLocation(saved.getFromLocation())
                .toLocation(saved.getToLocation())
                .transferredAt(saved.getTransferredAt())
                .purpose(saved.getPurpose())
                .sampleCondition(saved.getSampleCondition())
                .storageCondition(saved.getStorageCondition())
                .signatureToken(saved.getSignatureToken())
                .notes(saved.getNotes())
                .build();
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "SYSTEM";
    }
}
