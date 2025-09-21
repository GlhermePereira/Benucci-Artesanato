package br.edu.fatecpg.BenucciArtesanato.service;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.model.OrderItem;
import br.edu.fatecpg.BenucciArtesanato.model.Product;
import br.edu.fatecpg.BenucciArtesanato.model.User;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderRequestDTO;
import br.edu.fatecpg.BenucciArtesanato.repository.OrderRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.ProductRepository;
import br.edu.fatecpg.BenucciArtesanato.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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

        order.setStatus("pending");
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

}
