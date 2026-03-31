package com.ecommerce.order.client;

import com.ecommerce.order.config.ServicesConfig;
import com.ecommerce.order.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;

@Component
public class ProductClient{

    @Value("${services.product.url}")
    private String productServiceUrl;

    private final RestTemplate restTemplate;

    public ProductClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductDTO getProductById(String productId) {

        String url = productServiceUrl + "/products/{id}";
        try {
            return restTemplate.getForObject(url, ProductDTO.class, productId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Produto não encontrado: " + productId);
        }
    }

}
