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

import br.com.ecommerce.products.api.dto.department.SimpleDataDepartmentDTO;
import br.com.ecommerce.products.api.dto.department.UpdateDepartmentoDTO;
import br.com.ecommerce.products.api.mapper.CategoryMapper;
import br.com.ecommerce.products.api.mapper.DepartmentMapper;
import br.com.ecommerce.products.business.service.DepartmentService;
import br.com.ecommerce.products.business.validator.UniqueNameDepartmentValidator;
import br.com.ecommerce.products.infra.entity.department.Department;
import br.com.ecommerce.products.infra.exception.exceptions.DepartmentNotFoundException;
import br.com.ecommerce.products.infra.repository.DepartmentRepository;
import br.com.ecommerce.products.utils.builder.DepartmentTestBuilder;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceUnitTest {

    private final Department defaultDepartment = new DepartmentTestBuilder()
        .name("name")
        .build();

    @Mock
    private DepartmentRepository repository;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private UniqueNameDepartmentValidator uniqueNameDepartmentValidator;

    @InjectMocks
    private DepartmentService service;

    @Captor
    private ArgumentCaptor<Department> departmentCaptor; 


    @Test
    @DisplayName("Unit - getOneDepartment - Should throw an exception when not finding Department")
    void getOneDepartmentTest() {
        // act and assert
        assertThrows(DepartmentNotFoundException.class, () -> service.getOneDepartment(1L));
    }

    @Test
    @DisplayName("Unit - updateDepartment - Must update Department")
    void updateDepartmentTest01() {
        // arrange
        Department target = defaultDepartment;
        
        String newName = "newName";
        UpdateDepartmentoDTO requestBody = new UpdateDepartmentoDTO(newName);

        when(repository.findById(any()))
            .thenReturn(Optional.of(target));

        var response = new SimpleDataDepartmentDTO(1L, newName);
        when(departmentMapper.toSimpleDataDepartmentDTO(eq(target)))
            .thenReturn(response);
        
        // act
        SimpleDataDepartmentDTO result = service.updateDepartment(1L, requestBody);

        // assert
        assertEquals(requestBody.getName(), result.getName());
        verify(uniqueNameDepartmentValidator)
            .validate(eq(requestBody.getName()));

        verify(departmentMapper).toSimpleDataDepartmentDTO(departmentCaptor.capture());
        Department updated = departmentCaptor.getValue();
        assertEquals(requestBody.getName().toUpperCase(), updated.getName());
    }

    @Test
    @DisplayName("Unit - updateDepartment - Should throw exception when not finding Department")
    void updateDepartmentTest02() {
        // act and assert
        assertThrows(
            DepartmentNotFoundException.class,
            () -> service.updateDepartment(1L, new UpdateDepartmentoDTO()));
    }
}