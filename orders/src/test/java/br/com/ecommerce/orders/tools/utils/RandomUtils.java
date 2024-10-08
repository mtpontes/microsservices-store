package br.com.ecommerce.orders.tools.utils;

import java.math.BigDecimal;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class RandomUtils {

    private static Random random = new Random();

    
    public Long getRandomLong() {
        return Long.valueOf(random.nextInt(100));
    }

    public Integer getRandomInt() {
        return random.nextInt(1, 100);
    }
    public Integer getRandomInt(int value) {
        return random.nextInt(1, value);
    }

    public BigDecimal getRandomBigDecimal() {
        return BigDecimal.valueOf(random.nextInt(1, 100));
    }

    public String getRandomString() {
        return RandomStringUtils.randomAlphabetic(100);
    }
    public String getRandomString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    public String getRandomAlphanumericString() {
        return RandomStringUtils.randomAlphabetic(100);
    }
    public String getRandomAlphanumericString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }
}