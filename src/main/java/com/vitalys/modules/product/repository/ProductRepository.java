package com.vitalys.modules.product.repository;

import com.vitalys.modules.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    boolean existsByProductCodeAndIdNot(String productCode, Long id);

    @Query("SELECT p FROM Product p WHERE " +
           "(:search IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "   OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "   OR LOWER(p.registrationNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "   OR LOWER(p.manufacturer) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "   OR LOWER(p.dosageForm) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "AND (:status IS NULL OR p.status = :status)")
    Page<Product> searchProducts(@Param("search") String search,
                                 @Param("status") String status,
                                 Pageable pageable);
}
