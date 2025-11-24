package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.*;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderRequestDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.OrderRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.ProductRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Cria a entidade Order (para uso interno, ex: pagamento)
     */
    @Transactional
    public Order createOrderEntity(OrderRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Order order = new Order();
        order.setUser(user);
        order.setDeliveryType(request.getDeliveryType());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus(Order.OrderStatus.pending);

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderItem> items = new ArrayList<>();
        for (OrderRequestDTO.ItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + itemDTO.getProductId()));

            if (product.getStock() < itemDTO.getQuantity()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + product.getName());
            }

            // cria ID composto
            OrderItemId itemId = new OrderItemId();
            // ID de Order será gerado pelo Hibernate, então só vinculamos depois do save
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(itemDTO.getQuantity());

            items.add(item);

            // atualiza estoque
            product.setStock(product.getStock() - itemDTO.getQuantity());
            productRepository.save(product);

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        order.setItems(items);
        for (OrderItem item : items) {
            item.setOrder(order);
            // define o ID composto se necessário após o order ter ID
            OrderItemId id = new OrderItemId();
            id.setOrderId(order.getId()); // Hibernate preencherá na persistência
            id.setProductId(item.getProduct().getId());
            item.setId(id);
        }

        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }


    private static OrderItem getOrderItem(OrderRequestDTO.ItemDTO itemDTO, Product product, Order order) {
        if (product.getStock() < itemDTO.getQuantity()) {
            throw new RuntimeException("Estoque insuficiente para o produto: " + product.getName());
        }

        // Cria o ID composto
        OrderItemId itemId = new OrderItemId();
        itemId.setOrderId(order.getId());
        itemId.setProductId(product.getId());

        OrderItem item = new OrderItem();
        item.setId(itemId);
        item.setOrder(order);
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(itemDTO.getQuantity());
        return item;
    }


    /**
     * Cria um pedido e retorna o DTO (para controller)
     */
    @Transactional
    public OrderDTO createOrder(OrderRequestDTO request) {
        Order order = createOrderEntity(request); // usa o método de entidade
        return mapToDTO(order);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        return mapToDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByUser(Long userId) {
        List<Order> orders = orderRepository.findByUserIdWithItems(userId);
        return orders.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        order.setStatus(Order.OrderStatus.fromString(status));
        order.setUpdatedAt(OffsetDateTime.now());

        return mapToDTO(orderRepository.save(order));
    }

    /**
     * Converte a entidade Order em OrderDTO
     */
    private OrderDTO mapToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderDate(order.getCreatedAt().toLocalDateTime())
                .totalAmount(order.getTotalAmount())
                .deliveryType(order.getDeliveryType())
                .deliveryAddress(order.getDeliveryAddress())
                .status(order.getStatus().name())
                .items(order.getItems().stream().map(item ->
                        new br.edu.fatecpg.BenucciArtesanato.record.dto.OrderItemDTO(
                                item.getProduct().getId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        )
                ).collect(Collectors.toList()))
                .build();
    }
}
