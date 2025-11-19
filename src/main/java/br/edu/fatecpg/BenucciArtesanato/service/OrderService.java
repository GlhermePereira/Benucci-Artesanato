package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.model.OrderItem;
import br.edu.fatecpg.BenucciArtesanato.model.Product;
import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderItemDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderRequestDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.OrderRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.ProductRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public Order createOrder(OrderRequestDTO dto) {
        Order order = new Order();

        // Buscar usuário pelo ID
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        order.setUser(user);

        // Status inicial
        order.setStatus(Order.OrderStatus.pending);

        order.setDeliveryType(dto.getDeliveryType());
        order.setDeliveryAddress(dto.getDeliveryAddress());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequestDTO.ItemDTO itemDTO : dto.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDTO.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(itemDTO.getQuantity());

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            order.getItems().add(orderItem);
        }

        order.setTotalAmount(total);

        return orderRepository.save(order);
    }

    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderDetails(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        return OrderDTO.builder()
                .id(order.getId())
                .orderDate(order.getCreatedAt().toLocalDateTime())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name()) // retorna como String
                .items(order.getItems().stream().map(item ->
                        OrderItemDTO.builder()
                                .productId(item.getProduct().getId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdWithItems(userId);

        return orders.stream().map(order ->
                OrderDTO.builder()
                        .id(order.getId())
                        .orderDate(order.getCreatedAt().toLocalDateTime())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus().name())
                        .deliveryType(order.getDeliveryType())
                        .deliveryAddress(order.getDeliveryAddress())
                        .items(order.getItems().stream().map(item ->
                                OrderItemDTO.builder()
                                        .productId(item.getProduct().getId())
                                        .productName(item.getProductName())
                                        .quantity(item.getQuantity())
                                        .unitPrice(item.getUnitPrice())
                                        .build()
                        ).toList())
                        .build()
        ).toList();
    }

    public OrderDTO updateOrderStatus(Long orderId, String statusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        // Converte String para Enum usando o método seguro
        Order.OrderStatus status;
        try {
            status = Order.OrderStatus.fromString(statusStr);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status inválido: " + statusStr);
        }

        order.setStatus(status);
        orderRepository.save(order);

        return getOrderDetails(orderId);
    }
}
