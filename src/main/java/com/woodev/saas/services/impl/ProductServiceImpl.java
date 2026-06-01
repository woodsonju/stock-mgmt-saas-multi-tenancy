package com.woodev.saas.services.impl;

import com.woodev.saas.common.PageResponse;
import com.woodev.saas.entities.Category;
import com.woodev.saas.entities.Product;
import com.woodev.saas.exceptions.DuplicateResourceException;
import com.woodev.saas.mappers.ProductMapper;
import com.woodev.saas.repositories.CategoryRepository;
import com.woodev.saas.repositories.ProductRepository;
import com.woodev.saas.requests.ProductRequest;
import com.woodev.saas.responses.ProductResponse;
import com.woodev.saas.services.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public void create(final ProductRequest request) {
        // check if product already exists by reference
        checkIfProductAlreadyExistsByReference(request.getReference());
        // check if category exists by id
        checkIfCategoryExistsById(request.getCategoryId());
        final Product entity = this.productMapper.toEntity(request);
        this.productRepository.save(entity);
    }

    @Override
    public void update(final String id, final ProductRequest request) {
        // check if product already exists by id
        final Optional<Product> existingProduct = this.productRepository.findById(id);
        if(existingProduct.isEmpty()) {
            log.debug("Product does not exist");
            throw new EntityNotFoundException("Product does not exist");
        }

        // check if Product already exists by reference
        checkIfProductAlreadyExistsByReference(request.getReference());

        //check if category exists by id
        checkIfCategoryExistsById(request.getCategoryId());

        final Product productToUpdate = this.productMapper.toEntity(request);
        productToUpdate.setId(id); //we need to set the id of the product to update
        this.productRepository.save(productToUpdate);

    }

    @Override
    public PageResponse<ProductResponse> findAll(final int page, final int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Product> products = this.productRepository.findAll(pageRequest);
        final Page<ProductResponse> productResponsePage = products.map(this.productMapper::toResponse);
        return PageResponse.of(productResponsePage);
    }

    @Override
    public ProductResponse findById(String id) {
        return this.productRepository.findById(id)
                .map(this.productMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Product does not exist"));
    }

    @Override
    public void delete(final String id) {
        final Product product = this.productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product does not exist"));
        this.productRepository.delete(product);
    }

    private void checkIfProductAlreadyExistsByReference(final String reference) {
        final Optional<Product> product = this.productRepository.findByReferenceIgnoreCase(reference);
        if(product.isPresent()) {
            log.debug("Product already exists");
            throw new DuplicateResourceException("Product already exists"); //we will use custom exception later
        }
    }

    private void checkIfCategoryExistsById(final String categoryId) {
        final Optional<Category> category = this.categoryRepository.findById(categoryId);
        if(category.isEmpty()) {
            log.debug("Category does not exist");
            throw new EntityNotFoundException("Category does not exist");
        }
    }
}
