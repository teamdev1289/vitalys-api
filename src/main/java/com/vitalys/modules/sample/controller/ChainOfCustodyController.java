package com.vitalys.modules.sample.controller;

import com.vitalys.common.ResponseDto;
import com.vitalys.modules.sample.dto.CustodyLogResponse;
import com.vitalys.modules.sample.dto.CustodyTransferRequest;
import com.vitalys.modules.sample.service.ChainOfCustodyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Chain of Custody (Chuỗi giám sát mẫu) operations.
 */
@RestController
@RequestMapping("/api/v1/custody")
@RequiredArgsConstructor
@Tag(name = "Chain of Custody", description = "Endpoints for immutable sample custody logging and transfers")
@SecurityRequirement(name = "bearerAuth")
public class ChainOfCustodyController {

    private final ChainOfCustodyService custodyService;

    @Operation(summary = "Get complete Chain of Custody audit logs for a sample")
    @GetMapping("/sample/{sampleId}")
    @PreAuthorize("hasAuthority('SAMPLE:CUSTODY:READ')")
    public ResponseEntity<ResponseDto<List<CustodyLogResponse>>> getCustodyHistory(@PathVariable Long sampleId) {
        return ResponseEntity.ok(ResponseDto.ok(custodyService.getCustodyHistory(sampleId)));
    }

    @Operation(summary = "Transfer sample custody to another operator or storage location")
    @PostMapping("/sample/{sampleId}/transfer")
    @PreAuthorize("hasAuthority('SAMPLE:CUSTODY:TRANSFER')")
    public ResponseEntity<ResponseDto<CustodyLogResponse>> transferCustody(
            @PathVariable Long sampleId,
            @Valid @RequestBody CustodyTransferRequest request) {
        CustodyLogResponse transferred = custodyService.transferCustody(sampleId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.created("Sample custody transferred successfully", transferred));
    }
}
