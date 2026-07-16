package br.com.ecommerce.products.business.service;

import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.ecommerce.products.api.dto.department.CreateDepartmentDTO;
import br.com.ecommerce.products.api.dto.department.DataDepartmentDTO;
import br.com.ecommerce.products.api.dto.department.SimpleDataDepartmentDTO;
import br.com.ecommerce.products.api.dto.department.UpdateDepartmentoDTO;
import br.com.ecommerce.products.api.mapper.CategoryMapper;
import br.com.ecommerce.products.api.mapper.DepartmentMapper;
import br.com.ecommerce.products.business.validator.UniqueNameDepartmentValidator;
import br.com.ecommerce.products.infra.constants.CacheName;
import br.com.ecommerce.products.infra.entity.department.Department;
import br.com.ecommerce.products.infra.exception.exceptions.DepartmentNotFoundException;
import br.com.ecommerce.products.infra.repository.DepartmentRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final UniqueNameDepartmentValidator uniqueNameValidator;
    private final CategoryMapper categoryMapper;


    @Cacheable(cacheNames = CacheName.DEPARTMENTS, key = """
            #name + ':' +
            #pageable.pageNumber + ':' +
            #pageable.pageSize + ':'
            """)
    public Page<SimpleDataDepartmentDTO> getAllDepartments(String name, Pageable pageable) {
        return departmentRepository.findAllByParams(name, pageable)
            .map(departmentMapper::toSimpleDataDepartmentDTO);
    }

    public DataDepartmentDTO getOneDepartment(Long id) {
        return departmentRepository.findById(id)
            .map(department -> department.getCategories().stream()
                .map(categoryMapper::toSimpleDataCategoryDTO)
                .collect(Collectors.collectingAndThen(
                    Collectors.toList(), 
                    categories -> departmentMapper.toDataDepartmentDTO(department, categories))))
            .orElseThrow(DepartmentNotFoundException::new);
    }

    @Transactional
    public SimpleDataDepartmentDTO createDepartment(CreateDepartmentDTO data) {
        uniqueNameValidator.validate(data.getName());
        Department department = departmentMapper.toDepartment(data);
        departmentRepository.save(department);
        return departmentMapper.toSimpleDataDepartmentDTO(department);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheName.DEPARTMENTS, allEntries = true)
    public SimpleDataDepartmentDTO updateDepartment(Long id, UpdateDepartmentoDTO data) {
        uniqueNameValidator.validate(data.getName());
        return departmentRepository.findById(id)
            .map(department -> {
                department.update(data.getName());
                return department;
            })
            .map(departmentMapper::toSimpleDataDepartmentDTO)
            .orElseThrow(DepartmentNotFoundException::new);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheName.DEPARTMENTS, allEntries = true)
    public void destroyDepartment(Long id) {
        departmentRepository.deleteById(id);
    }
}