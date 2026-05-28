package com.woodev.saas.repositories;

import com.woodev.saas.entities.Category;
import com.woodev.saas.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findByReferenceIgnoreCase(String reference);
}
