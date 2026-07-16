package br.com.ecommerce.products.unit.business.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import br.com.ecommerce.products.api.dto.manufacturer.DataManufacturerDTO;
import br.com.ecommerce.products.api.dto.manufacturer.UpdateManufacturerDTO;
import br.com.ecommerce.products.api.mapper.AddressMapper;
import br.com.ecommerce.products.api.mapper.ManufacturerMapper;
import br.com.ecommerce.products.business.service.ManufacturerService;
import br.com.ecommerce.products.business.validator.UniqueNameManufacturerValidator;
import br.com.ecommerce.products.infra.entity.manufacturer.Manufacturer;
import br.com.ecommerce.products.infra.entity.tools.factory.PhoneFactory;
import br.com.ecommerce.products.infra.exception.exceptions.ManufacturerNotFoundException;
import br.com.ecommerce.products.infra.repository.ManufacturerRepository;
import br.com.ecommerce.products.utils.builder.ManufacturerTestBuilder;

@ExtendWith(MockitoExtension.class)
class ManufacturerServiceUnitTest {

    private final Manufacturer defaultManufacturer = new ManufacturerTestBuilder()
        .name("name")
        .build();

    @Mock
    private ManufacturerRepository repositoryMock;
    @Mock
    private PhoneFactory phoneFactoryMock;
    @Mock
    private ManufacturerMapper mapperMock;
    @Mock
    private AddressMapper addressMapperMock;
    @Mock
    private UniqueNameManufacturerValidator uniqueNameValidatorMock;

    @InjectMocks
    private ManufacturerService service;

    @Captor
    private ArgumentCaptor<Manufacturer> manufacturerCaptor; 


    @Test
    @DisplayName("Unit - getManufacturer - Must throw exception")
    void getManufacturerTest() {
        // act and assert
        assertThrows(ManufacturerNotFoundException.class, () -> service.getManufacturer(1L));
    }

    @Test
    @DisplayName("Unit - updateManufacturer - Must update manufacturer")
    void updateManufacturerTest01() {
        // arrange
        Manufacturer target = defaultManufacturer;
        
        String newName = "newName";
        UpdateManufacturerDTO requestBody = new UpdateManufacturerDTO(
            newName,
            "",
            "",
            "",
            null
        );

        when(repositoryMock.findById(any()))
            .thenReturn(Optional.of(target));

        var response = new DataManufacturerDTO(
            null, newName, null, null, null, null);
        when(mapperMock.toDataManufacturerDTO(eq(target), any()))
            .thenReturn(response);
        
        // act
        DataManufacturerDTO result = service.updateManufacturer(1L, requestBody);

        // assert
        assertEquals(requestBody.getName(), result.getName());
        verify(uniqueNameValidatorMock)
            .validate(eq(requestBody.getName()));

        verify(mapperMock).toDataManufacturerDTO(manufacturerCaptor.capture(), any());
        Manufacturer updated = manufacturerCaptor.getValue();
        assertEquals(requestBody.getName().toUpperCase(), updated.getName());
    }

    @Test
    @DisplayName("Unit - updateManufacturer - Should throw exception when not finding manufacturer")
    void updateManufacturerTest02() {
        // act and assert
        assertThrows(
            ManufacturerNotFoundException.class,
            () -> service.updateManufacturer(1L, new UpdateManufacturerDTO()));
    }
}