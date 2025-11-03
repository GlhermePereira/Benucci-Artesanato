package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.model.Payment;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderRequestDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderResponseDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.UpdateOrderStatusDTO;
import br.edu.fatecpg.BenucciArtesanato.service.OrderService;
import br.edu.fatecpg.BenucciArtesanato.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsável pelo gerenciamento de pedidos.
 * <p>
 * Endpoints disponíveis:
 * - Criar pedido
 * - Consultar pedido por ID
 * - Consultar pedidos de um usuário
 * - Atualizar status de pedido
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    /**
     * Cria um novo pedido e gera o pagamento associado.
     *
     * @param request DTO com os dados do pedido
     * @return DTO com informações do pedido e link de checkout
     * @throws Exception caso ocorra erro na criação do pedido ou pagamento
     */
    @Operation(summary = "Cria um novo pedido", description = "Cria um pedido e retorna link de pagamento e status inicial")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados do pedido inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO request) throws Exception {
        log.info("Criando novo pedido para usuário: {}", request.getUserId());

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

    /**
     * Consulta um pedido pelo seu ID.
     *
     * @param id ID do pedido
     * @return DTO com informações detalhadas do pedido
     */
    @Operation(summary = "Consulta pedido por ID", description = "Retorna detalhes de um pedido específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        log.info("Consultando pedido com ID: {}", id);
        return ResponseEntity.ok(orderService.getOrderDetails(id));
    }

    /**
     * Lista todos os pedidos de um usuário.
     *
     * @param userId ID do usuário
     * @return Lista de pedidos do usuário
     */
    @Operation(summary = "Lista pedidos de um usuário", description = "Retorna todos os pedidos feitos por um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos retornados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@PathVariable Long userId) {
        log.info("Consultando pedidos do usuário com ID: {}", userId);
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    /**
     * Atualiza o status de um pedido.
     *
     * @param id  ID do pedido
     * @param dto DTO contendo o novo status
     * @return DTO com informações atualizadas do pedido
     */
    @Operation(summary = "Atualiza status do pedido", description = "Permite alterar o status de um pedido existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do pedido atualizado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "400", description = "Status inválido")
    })
    @PutMapping("/status/{id}")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusDTO dto) {

        log.info("Atualizando status do pedido ID {} para {}", id, dto.getStatus());
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, dto.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }
}
