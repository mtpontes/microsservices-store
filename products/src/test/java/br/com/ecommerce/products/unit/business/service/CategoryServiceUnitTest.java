package br.com.ecommerce.products.unit.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ecommerce.products.api.dto.category.SimpleDataCategoryDTO;
import br.com.ecommerce.products.api.dto.category.UpdateCategoryDTO;
import br.com.ecommerce.products.api.mapper.CategoryMapper;
import br.com.ecommerce.products.business.service.CategoryService;
import br.com.ecommerce.products.business.validator.UniqueNameCategoryValidator;
import br.com.ecommerce.products.infra.entity.category.Category;
import br.com.ecommerce.products.infra.exception.exceptions.CategoryNotFoundException;
import br.com.ecommerce.products.infra.repository.CategoryRepository;
import br.com.ecommerce.products.infra.repository.DepartmentRepository;
import br.com.ecommerce.products.utils.builder.CategoryTestBuilder;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {

    private final Category defaultCategory = new CategoryTestBuilder()
        .name("name")
        .build();

    @Mock
    private CategoryRepository repository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private CategoryMapper mapper;
    @Mock
    private UniqueNameCategoryValidator uniqueNameCategoryValidator;

    @InjectMocks
    private CategoryService service;

    @Captor
    private ArgumentCaptor<Category> categoryCaptor; 


    @Test
    @DisplayName("Unit - getOne - Should throw an exception when not finding Category")
    void getOneTest() {
        // act and assert
        assertThrows(CategoryNotFoundException.class, () -> service.getOne(1L));
    }

    @Test
    @DisplayName("Unit - update - Must update Category")
    void updateTest01() {
        // arrange
        Category target = defaultCategory;
        
        String newName = "newName";
        UpdateCategoryDTO requestBody = new UpdateCategoryDTO(newName);

        when(repository.findById(any()))
            .thenReturn(Optional.of(target));

        var response = new SimpleDataCategoryDTO(1L, newName);
        when(mapper.toSimpleDataCategoryDTO(eq(target)))
            .thenReturn(response);
        
        // act
        SimpleDataCategoryDTO result = service.update(1L, requestBody);

        // assert
        assertEquals(requestBody.getName(), result.getName());
        verify(uniqueNameCategoryValidator)
            .validate(eq(requestBody.getName()));

        verify(mapper).toSimpleDataCategoryDTO(categoryCaptor.capture());
        Category updated = categoryCaptor.getValue();
        assertEquals(requestBody.getName().toUpperCase(), updated.getName());
    }

    @Test
    @DisplayName("Unit - update - Should throw exception when not finding Category")
    void updateTest02() {
        // act and assert
        assertThrows(CategoryNotFoundException.class, () -> service.update(1L, new UpdateCategoryDTO()));
    }
}