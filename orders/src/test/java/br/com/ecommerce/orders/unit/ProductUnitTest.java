package br.com.ecommerce.orders.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.ecommerce.orders.infra.entity.Product;

class ProductUnitTest {

    private final String PRODUCT_ID = "1";
    private final String NAME = "Name";
    private final BigDecimal PRICE = BigDecimal.valueOf(100);
    private final int UNITS = 10;


    @Test
    @DisplayName("Test creating an order with a valid product")
    void createProductTest01() {
        assertDoesNotThrow(() -> new Product(PRODUCT_ID, NAME, PRICE, UNITS));
    }

    @Test
    @DisplayName("Test creating product with various invalid inputs")
    void createProductTest02() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Product(null, NAME,  PRICE, UNITS),
            "Creating product with null ID should throw IllegalArgumentException");
        
        assertThrows(IllegalArgumentException.class, 
            () -> new Product(PRODUCT_ID, NAME, null, UNITS),
            "Creating product with null price should throw IllegalArgumentException");

        assertThrows(IllegalArgumentException.class, 
            () -> new Product(PRODUCT_ID, NAME, BigDecimal.valueOf(-100), UNITS),
            "Creating product with null price should throw IllegalArgumentException");
        
        assertThrows(IllegalArgumentException.class, 
            () -> new Product(PRODUCT_ID, NAME, PRICE, null),
            "Creating product with null quantity should throw IllegalArgumentException");

        assertThrows(IllegalArgumentException.class, 
            () -> new Product(PRODUCT_ID, NAME, PRICE, -100),
            "Creating product with null quantity should throw IllegalArgumentException");
    }
}