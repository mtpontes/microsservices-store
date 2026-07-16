package br.com.ecommerce.products.business.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.ecommerce.products.api.dto.category.CreateCategoryDTO;
import br.com.ecommerce.products.api.dto.category.SimpleDataCategoryDTO;
import br.com.ecommerce.products.api.dto.category.UpdateCategoryDTO;
import br.com.ecommerce.products.api.mapper.CategoryMapper;
import br.com.ecommerce.products.business.validator.UniqueNameCategoryValidator;
import br.com.ecommerce.products.infra.constants.CacheName;
import br.com.ecommerce.products.infra.entity.category.Category;
import br.com.ecommerce.products.infra.exception.exceptions.CategoryNotFoundException;
import br.com.ecommerce.products.infra.exception.exceptions.DepartmentNotFoundException;
import br.com.ecommerce.products.infra.repository.CategoryRepository;
import br.com.ecommerce.products.infra.repository.DepartmentRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final CategoryMapper categoryMapper;
    private final UniqueNameCategoryValidator uniqueNameValidator;


    @Transactional
    public SimpleDataCategoryDTO create(CreateCategoryDTO dto) {
        uniqueNameValidator.validate(dto.getName());
        return departmentRepository.findById(dto.getDepartmentId())
            .map(dp -> {
                Category category = categoryMapper.toCategory(dto.getName(), dp);
                categoryRepository.save(category);

                dp.addCategory(category);
                departmentRepository.save(dp);

                return category;
            })
            .map(categoryMapper::toSimpleDataCategoryDTO)
            .orElseThrow(DepartmentNotFoundException::new);
    }

    @Cacheable(cacheNames = CacheName.CATEGORIES, key = """
            #root.methodName + ':' +
            #name + ':' +
            #pageable.pageNumber + ':' +
            #pageable.pageSize + ':'
            """)
    public Page<SimpleDataCategoryDTO> getAllByParams(
        String name, 
        Pageable pageable
    ) {
        return categoryRepository.findAllByParams(name, pageable)
            .map(categoryMapper::toSimpleDataCategoryDTO);
    }

    public SimpleDataCategoryDTO getOne(Long id) {
        return categoryRepository.findById(id)
            .map(categoryMapper::toSimpleDataCategoryDTO)
            .orElseThrow(CategoryNotFoundException::new);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheName.CATEGORIES, allEntries = true)
    public SimpleDataCategoryDTO update(Long id, UpdateCategoryDTO dto) {
        uniqueNameValidator.validate(dto.getName());
        return categoryRepository.findById(id)
            .map(category -> {
                category.update(dto.getName());
                return category;
            })
            .map(categoryMapper::toSimpleDataCategoryDTO)
            .orElseThrow(CategoryNotFoundException::new);
    }
}