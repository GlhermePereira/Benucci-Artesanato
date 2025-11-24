package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.model.Order;
import br.edu.fatecpg.BenucciArtesanato.record.dto.OrderRequestDTO;
import br.edu.fatecpg.BenucciArtesanato.record.dto.PaymentResponseDTO;
import br.edu.fatecpg.BenucciArtesanato.service.OrderService;
import br.edu.fatecpg.BenucciArtesanato.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @Operation(summary = "Cria preferência de pagamento", description = "Cria pedido e retorna link de checkout do Mercado Pago")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferência criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/preference")
    public ResponseEntity<PaymentResponseDTO> createPaymentPreference(@RequestBody OrderRequestDTO requestDTO) {
        try {
            log.info("Criando pedido e preferência de pagamento para usuário ID: {}", requestDTO.getUserId());

            // Cria a entidade Order
            Order order = orderService.createOrderEntity(requestDTO);

            // Cria a preferência de pagamento
            PaymentResponseDTO paymentResponse = paymentService.createPayment(order);

            return ResponseEntity.ok(paymentResponse);

        } catch (Exception e) {
            log.error("Erro ao criar preferência de pagamento: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(null);
        }
    }
}
