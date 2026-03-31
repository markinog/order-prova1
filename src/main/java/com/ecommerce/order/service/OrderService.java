package com.ecommerce.order.service;

import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.client.PaymentClient;
import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.DecreaseStockRequest;
import com.ecommerce.order.dto.InventoryDTO;
import com.ecommerce.order.dto.OrderItemRequest;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.PaymentRequestDTO;
import com.ecommerce.order.dto.PaymentResponseDTO;
import com.ecommerce.order.dto.ProductDTO;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate, ProductClient productClient, InventoryClient inventoryClient, PaymentClient paymentClient) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
    }

    public Order createOrder(OrderRequest request) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {

            ProductDTO product = productClient.getProductById(itemRequest.getProductId());

            if (product == null) {
                throw new RuntimeException("Produto não encontrado: " + itemRequest.getProductId());
            }

            InventoryDTO inventory = inventoryClient.getInventoryByProductId(itemRequest.getProductId());

            if (inventory == null || inventory.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Estoque insuficiente para produto: " + product.getNome());
            }

            BigDecimal subtotal = product.getPreco().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getNome());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPreco());
            orderItem.setSubtotal(subtotal);

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }


        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CRIADO);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        PaymentRequestDTO paymentRequest = new PaymentRequestDTO();
        paymentRequest.setOrderId(savedOrder.getId());
        paymentRequest.setAmount(totalAmount);

        PaymentResponseDTO paymentResponse = paymentClient.processPayment(paymentRequest);

        if (paymentResponse == null) {
            throw new RuntimeException("Erro ao processar pagamento");
        }

        savedOrder.setPaymentId(paymentResponse.getId());

        if ("APROVADO".equals(paymentResponse.getStatus())) {
            savedOrder.setStatus(OrderStatus.PAGO);

            for (OrderItem item : orderItems) {
                inventoryClient.decreaseStock(item.getProductId(), item.getQuantity());
            }

        } else {
            savedOrder.setStatus(OrderStatus.CANCELADO);
        }

        return orderRepository.save(savedOrder);
    }

    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }
}
