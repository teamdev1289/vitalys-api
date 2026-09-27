package com.vitalys.modules.product.repository;

import com.vitalys.modules.product.entity.ProductActiveIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductActiveIngredientRepository extends JpaRepository<ProductActiveIngredient, Long> {
}
