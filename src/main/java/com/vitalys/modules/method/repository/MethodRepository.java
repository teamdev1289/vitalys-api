package com.vitalys.modules.method.repository;

import com.vitalys.modules.method.entity.Method;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface MethodRepository extends JpaRepository<Method, Long> {
    Optional<Method> findByMethodCode(String methodCode);
    List<Method> findByValidationStatus(String validationStatus);
    List<Method> findByCategory(String category);
    List<Method> findByIsActiveTrue();

    @Query("SELECT m FROM Method m WHERE " +
           "(:keyword IS NULL OR LOWER(m.methodCode) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           " LOWER(m.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           " LOWER(m.sourceStandard) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) AND " +
           "(:category IS NULL OR m.category = :category) AND " +
           "(:validationStatus IS NULL OR m.validationStatus = :validationStatus)")
    List<Method> searchMethods(@Param("keyword") String keyword,
                               @Param("category") String category,
                               @Param("validationStatus") String validationStatus);
}
