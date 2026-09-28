package com.vitalys.modules.sys.service;

import jakarta.persistence.EntityNotFoundException;
import com.vitalys.modules.sys.dto.department.DepartmentRequest;
import com.vitalys.modules.sys.dto.department.DepartmentResponse;
import com.vitalys.modules.sys.entity.SysDepartment;
import com.vitalys.modules.sys.repository.SysDepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private SysDepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private SysDepartment sampleDept;

    @BeforeEach
    void setUp() {
        sampleDept = SysDepartment.builder()
                .id(1L)
                .code("BIOCHEM")
                .name("Biochemistry Laboratory")
                .description("Chemical testing")
                .build();
    }

    @Test
    @DisplayName("getAllDepartments - returns all departments as DTOs")
    void getAllDepartments_success() {
        when(departmentRepository.findAll()).thenReturn(List.of(sampleDept));

        List<DepartmentResponse> result = departmentService.getAllDepartments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("BIOCHEM");
        verify(departmentRepository).findAll();
    }

    @Test
    @DisplayName("getDepartmentById - returns department when exists")
    void getDepartmentById_found() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDept));

        DepartmentResponse result = departmentService.getDepartmentById(1L);

        assertThat(result.getName()).isEqualTo("Biochemistry Laboratory");
    }

    @Test
    @DisplayName("getDepartmentById - throws ResourceNotFoundException when missing")
    void getDepartmentById_notFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getDepartmentById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Department not found");
    }

    @Test
    @DisplayName("createDepartment - creates and saves new department")
    void createDepartment_success() {
        DepartmentRequest request = DepartmentRequest.builder()
                .code("QC_GEN")
                .name("General Quality Control")
                .description("Routine lab analysis")
                .build();

        when(departmentRepository.existsByCode("QC_GEN")).thenReturn(false);
        when(departmentRepository.save(any(SysDepartment.class))).thenAnswer(inv -> {
            SysDepartment arg = inv.getArgument(0);
            arg.setId(10L);
            return arg;
        });

        DepartmentResponse result = departmentService.createDepartment(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getCode()).isEqualTo("QC_GEN");
        verify(departmentRepository).save(any(SysDepartment.class));
    }
}
