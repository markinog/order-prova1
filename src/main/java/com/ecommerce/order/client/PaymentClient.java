package com.ecommerce.order.client;

import com.ecommerce.order.config.ServicesConfig;
import com.ecommerce.order.dto.PaymentRequestDTO;
import com.ecommerce.order.dto.PaymentResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentClient {

    @Value("${services.payment.url}")
    private String paymentServiceUrl;

    private final RestTemplate restTemplate;

    public PaymentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequest) {
        String url = paymentServiceUrl + "/payments";

        PaymentResponseDTO response = restTemplate.postForObject(url, paymentRequest, PaymentResponseDTO.class);

        if (response == null) {
            throw new RuntimeException("Erro ao processar pagamento");
        }

        return response;
    }
}
