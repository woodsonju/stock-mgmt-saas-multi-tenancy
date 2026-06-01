package com.woodev.saas.services.impl;

import com.woodev.saas.common.PageResponse;
import com.woodev.saas.entities.Category;
import com.woodev.saas.mappers.CategoryMapper;
import com.woodev.saas.repositories.CategoryRepository;
import com.woodev.saas.requests.CategoryRequest;
import com.woodev.saas.responses.CategoryResponse;
import com.woodev.saas.services.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public void create(final CategoryRequest request) {
        checkIfCategoryAlreadyExistsByName(request.getName());
        final Category entity = this.categoryMapper.toEntity(request);
        this.categoryRepository.save(entity);
    }



    @Override
    public void update(final String id, final CategoryRequest request) {
        //check if category already exists by ID
        final Optional<Category> existingCategory = this.categoryRepository.findById(id);
        if(existingCategory.isEmpty()) {
            log.debug("Category does not exist ");
            throw new EntityNotFoundException("Category does not exist"); //we will use custom exception later
        }

        //check if category already exists by name
        checkIfCategoryAlreadyExistsByName(request.getName());

        //Qu'est ce qu'on pourrait faire ?
        //On peut dans la méthode mapper créer une autre entité qui s'appelle merge entity
        //qui vérifie que les objets ou les informations qu'on a reçu sont conformes
        //ou bien on essaie de merger ou bien de fusionner les informations
        final Category categoryToUpdate = this.categoryMapper.toEntity(request);
        categoryToUpdate.setId(id);
        this.categoryRepository.save(categoryToUpdate);

    }

    @Override
    public PageResponse<CategoryResponse> findAll(final int page, final int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Category> categories = this.categoryRepository.findAll(pageRequest);
        final Page<CategoryResponse> categoryResponsePage = categories.map(this.categoryMapper::toResponse);
        return PageResponse.of(categoryResponsePage);
    }

    @Override
    public CategoryResponse findById(String id) {
        return this.categoryRepository.findById(id)
                .map(this.categoryMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Category does not exist"));

    }

    @Override
    public void delete(String id) {
        final Category category =  this.categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category does not exist"));
        this.categoryRepository.delete(category);

    }

    private void checkIfCategoryAlreadyExistsByName(final String categoryName) {
        //check if category already exists
        final Optional<Category> category = this.categoryRepository.findByNameIgnoreCase(categoryName);
        if(category.isPresent()) {
            log.debug("Category already exists");
            throw new RuntimeException("Category already exists"); //we will use custom exception later
        }
    }
}
