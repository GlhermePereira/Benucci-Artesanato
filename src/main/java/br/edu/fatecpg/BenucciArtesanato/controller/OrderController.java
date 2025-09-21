package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.model.Payment;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderRequestDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderResponseDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.UpdateOrderStatusDTO;
import br.edu.fatecpg.BenucciArtesanato.service.OrderService;
import br.edu.fatecpg.BenucciArtesanato.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO request) throws Exception {
        Order order = orderService.createOrder(request);
        Payment payment = paymentService.createPayment(order);
        OrderResponseDTO response = new OrderResponseDTO(
                order.getId(),
                payment.getMpPreferenceId(), // link de checkout
                order.getStatus().name(),
                payment.getStatus()
        );



        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderDetails(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(orderService.getUserOrders(usuarioId));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusDTO dto) {
        System.out.println("Chamou updateOrderStatus para id = " + id + " com status = " + dto.getStatus());
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, dto.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }

}
