package com.ecommerce.order.client;

import com.ecommerce.order.dto.DecreaseStockRequest;
import com.ecommerce.order.dto.InventoryDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InventoryClient {

    @Value("${services.inventory.url}")
    private String inventoryServiceUrl;

    private final RestTemplate restTemplate;

    public InventoryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public InventoryDTO getInventoryByProductId(String productId) {
        String url = inventoryServiceUrl + "/inventory/" + productId;

        InventoryDTO inventory = restTemplate.getForObject(url, InventoryDTO.class);

        if (inventory == null) {
            throw new RuntimeException("Estoque não encontrado para produto: " + productId);
        }

        return inventory;
    }

    public void decreaseStock(String productId, Integer quantity) {
        String url = inventoryServiceUrl + "/inventory/" + productId + "/decrease";

        DecreaseStockRequest request = new DecreaseStockRequest(quantity);
        restTemplate.postForObject(url, request, Void.class);
    }


}
