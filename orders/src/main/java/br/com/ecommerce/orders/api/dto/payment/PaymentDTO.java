package br.com.ecommerce.orders.api.dto.payment;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private String orderId;
    private String userId;
    private BigDecimal paymentAmount;
}