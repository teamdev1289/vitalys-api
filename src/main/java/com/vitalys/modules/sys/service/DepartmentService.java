package com.vitalys.modules.sys.service;

import jakarta.persistence.EntityNotFoundException;
import com.vitalys.modules.sys.annotation.Auditable;
import com.vitalys.modules.sys.dto.department.DepartmentRequest;
import com.vitalys.modules.sys.dto.department.DepartmentResponse;
import com.vitalys.modules.sys.entity.SysDepartment;
import com.vitalys.modules.sys.repository.SysDepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final SysDepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        SysDepartment dept = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));
        return toResponse(dept);
    }

    @Auditable(module = "SYS", entity = "Department")
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Department with code " + request.getCode() + " already exists");
        }
        SysDepartment dept = SysDepartment.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();
        SysDepartment saved = departmentRepository.save(dept);
        log.info("Created new department: {} ({})", saved.getName(), saved.getCode());
        return toResponse(saved);
    }

    @Auditable(module = "SYS", entity = "Department")
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        SysDepartment dept = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));

        dept.setName(request.getName().trim());
        dept.setDescription(request.getDescription());
        SysDepartment updated = departmentRepository.save(dept);
        log.info("Updated department ID {}: {}", id, updated.getName());
        return toResponse(updated);
    }

    @Auditable(module = "SYS", entity = "Department")
    @Transactional
    public void deleteDepartment(Long id) {
        SysDepartment dept = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found with id: " + id));
        departmentRepository.delete(dept);
        log.info("Deleted department ID {}: {}", id, dept.getName());
    }

    private DepartmentResponse toResponse(SysDepartment dept) {
        return DepartmentResponse.builder()
                .id(dept.getId())
                .code(dept.getCode())
                .name(dept.getName())
                .description(dept.getDescription())
                .build();
    }
}
