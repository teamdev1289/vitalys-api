package com.vitalys.modules.partner.service;

import com.vitalys.modules.partner.dto.*;
import com.vitalys.modules.partner.entity.Customer;
import com.vitalys.modules.partner.entity.Project;
import com.vitalys.modules.partner.repository.CustomerRepository;
import com.vitalys.modules.partner.repository.ProjectRepository;
import com.vitalys.modules.sys.annotation.Auditable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service managing client accounts (Customer) and contract analytical projects (Project).
 * All mutating actions are logged for GxP compliance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerService {

    private final CustomerRepository customerRepository;
    private final ProjectRepository projectRepository;

    // ── Customer Operations ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<CustomerResponse> getCustomers(String search, String status, Pageable pageable) {
        Page<Customer> page = customerRepository.searchCustomers(search, status, pageable);

        // Precompute project counts for page items
        List<Long> customerIds = page.getContent().stream().map(Customer::getId).toList();
        Map<Long, Integer> projectCountMap = customerIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> projectRepository.findByCustomerId(id).size()
                ));

        return page.map(c -> toCustomerResponse(c, projectCountMap.getOrDefault(c.getId(), 0)));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = findCustomerOrThrow(id);
        int projectCount = projectRepository.findByCustomerId(id).size();
        return toCustomerResponse(customer, projectCount);
    }

    @Auditable(module = "PARTNER", entity = "Customer")
    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        if (customerRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Customer code already exists: " + request.getCode());
        }

        Customer customer = Customer.builder()
                .code(request.getCode())
                .name(request.getName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(request.getAddress())
                .taxId(request.getTaxId())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .notes(request.getNotes())
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Registered customer ID {}: {} [{}]", saved.getId(), saved.getName(), saved.getCode());
        return toCustomerResponse(saved, 0);
    }

    @Auditable(module = "PARTNER", entity = "Customer")
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request) {
        Customer customer = findCustomerOrThrow(id);

        if (customerRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new IllegalArgumentException("Customer code is already used by another client: " + request.getCode());
        }

        customer.setCode(request.getCode());
        customer.setName(request.getName());
        customer.setContactEmail(request.getContactEmail());
        customer.setContactPhone(request.getContactPhone());
        customer.setAddress(request.getAddress());
        customer.setTaxId(request.getTaxId());
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }
        customer.setNotes(request.getNotes());

        Customer updated = customerRepository.save(customer);
        int projectCount = projectRepository.findByCustomerId(id).size();
        log.info("Updated customer ID {}: {} [{}]", updated.getId(), updated.getName(), updated.getCode());
        return toCustomerResponse(updated, projectCount);
    }

    @Auditable(module = "PARTNER", entity = "Customer")
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = findCustomerOrThrow(id);
        List<Project> projects = projectRepository.findByCustomerId(id);
        if (!projects.isEmpty()) {
            throw new IllegalStateException("Cannot delete customer with active projects. Archive projects first.");
        }
        customerRepository.delete(customer);
        log.info("Deleted customer ID {}: {}", id, customer.getName());
    }

    // ── Project Operations ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjects(String search, Long customerId, String status, Pageable pageable) {
        Page<Project> page = projectRepository.searchProjects(search, customerId, status, pageable);

        Map<Long, Customer> customerMap = customerRepository.findAll().stream()
                .collect(Collectors.toMap(Customer::getId, c -> c, (a, b) -> a));

        return page.map(p -> toProjectResponse(p, customerMap.get(p.getCustomerId())));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = findProjectOrThrow(id);
        Customer customer = customerRepository.findById(project.getCustomerId()).orElse(null);
        return toProjectResponse(project, customer);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByCustomer(Long customerId) {
        Customer customer = findCustomerOrThrow(customerId);
        return projectRepository.findByCustomerId(customerId).stream()
                .map(p -> toProjectResponse(p, customer))
                .collect(Collectors.toList());
    }

    @Auditable(module = "PARTNER", entity = "Project")
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        Customer customer = findCustomerOrThrow(request.getCustomerId());

        if (projectRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Project code already exists: " + request.getCode());
        }

        Project project = Project.builder()
                .customerId(customer.getId())
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .notes(request.getNotes())
                .build();

        Project saved = projectRepository.save(project);
        log.info("Created project ID {}: {} [{}] for customer {}", saved.getId(), saved.getName(), saved.getCode(), customer.getName());
        return toProjectResponse(saved, customer);
    }

    @Auditable(module = "PARTNER", entity = "Project")
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectUpdateRequest request) {
        Project project = findProjectOrThrow(id);
        Customer customer = findCustomerOrThrow(request.getCustomerId());

        if (projectRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new IllegalArgumentException("Project code already in use: " + request.getCode());
        }

        project.setCustomerId(customer.getId());
        project.setCode(request.getCode());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setNotes(request.getNotes());

        Project updated = projectRepository.save(project);
        log.info("Updated project ID {}: {} [{}]", updated.getId(), updated.getName(), updated.getCode());
        return toProjectResponse(updated, customer);
    }

    @Auditable(module = "PARTNER", entity = "Project")
    @Transactional
    public void deleteProject(Long id) {
        Project project = findProjectOrThrow(id);
        projectRepository.delete(project);
        log.info("Deleted project ID {}: {}", id, project.getName());
    }

    // ── Helper Mappers ────────────────────────────────────────────────────────

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + id));
    }

    private Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + id));
    }

    private CustomerResponse toCustomerResponse(Customer c, int activeProjectCount) {
        return CustomerResponse.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .contactEmail(c.getContactEmail())
                .contactPhone(c.getContactPhone())
                .address(c.getAddress())
                .taxId(c.getTaxId())
                .status(c.getStatus())
                .notes(c.getNotes())
                .activeProjectCount(activeProjectCount)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ProjectResponse toProjectResponse(Project p, Customer c) {
        return ProjectResponse.builder()
                .id(p.getId())
                .customerId(p.getCustomerId())
                .customerCode(c != null ? c.getCode() : null)
                .customerName(c != null ? c.getName() : null)
                .code(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .status(p.getStatus())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .notes(p.getNotes())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
