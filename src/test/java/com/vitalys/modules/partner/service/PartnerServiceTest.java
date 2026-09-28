package com.vitalys.modules.partner.service;

import com.vitalys.modules.partner.dto.*;
import com.vitalys.modules.partner.entity.Customer;
import com.vitalys.modules.partner.entity.Project;
import com.vitalys.modules.partner.repository.CustomerRepository;
import com.vitalys.modules.partner.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartnerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private PartnerService partnerService;

    private Customer sampleCustomer;
    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleCustomer = Customer.builder()
                .id(1L)
                .code("CUST-DHG-001")
                .name("Hau Giang Pharma")
                .contactEmail("qc@dhgpharma.com.vn")
                .status("ACTIVE")
                .build();

        sampleProject = Project.builder()
                .id(10L)
                .customerId(1L)
                .code("PRJ-DHG-001")
                .name("Stability Program 2026")
                .status("ACTIVE")
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
    }

    @Test
    @DisplayName("createCustomer - creates customer successfully")
    void testCreateCustomer_Success() {
        CustomerCreateRequest request = CustomerCreateRequest.builder()
                .code("CUST-DHG-001")
                .name("Hau Giang Pharma")
                .contactEmail("qc@dhgpharma.com.vn")
                .build();

        when(customerRepository.existsByCode("CUST-DHG-001")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        CustomerResponse response = partnerService.createCustomer(request);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("CUST-DHG-001");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("createCustomer - duplicate code throws error")
    void testCreateCustomer_DuplicateCode() {
        CustomerCreateRequest request = CustomerCreateRequest.builder()
                .code("CUST-DHG-001")
                .name("Another Pharma")
                .build();

        when(customerRepository.existsByCode("CUST-DHG-001")).thenReturn(true);

        assertThatThrownBy(() -> partnerService.createCustomer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer code already exists");

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProject - creates project for existing customer")
    void testCreateProject_Success() {
        ProjectCreateRequest request = ProjectCreateRequest.builder()
                .customerId(1L)
                .code("PRJ-DHG-001")
                .name("Stability Program 2026")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(projectRepository.existsByCode("PRJ-DHG-001")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(sampleProject);

        ProjectResponse response = partnerService.createProject(request);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("PRJ-DHG-001");
        assertThat(response.getCustomerCode()).isEqualTo("CUST-DHG-001");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("getCustomers - returns paginated responses with project count")
    void testGetCustomers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> page = new PageImpl<>(List.of(sampleCustomer), pageable, 1);

        when(customerRepository.searchCustomers(null, null, pageable)).thenReturn(page);
        when(projectRepository.findByCustomerId(1L)).thenReturn(List.of(sampleProject));

        Page<CustomerResponse> result = partnerService.getCustomers(null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getActiveProjectCount()).isEqualTo(1);
    }
}
