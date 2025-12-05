package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.service.PaymentWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Endpoints para recebimento de webhooks externos, incluindo Mercado Pago")
public class WebHookController {

    private final PaymentWebhookService paymentWebhookService;

    @Operation(
            summary = "Receber Webhook do Mercado Pago",
            description = "Recebe notificações reais enviadas pelo Mercado Pago e delega para o serviço responsável pelo processamento."
    )
    @PostMapping("/mercadopago")
    public ResponseEntity<String> receiveMercadoPagoWebhook(
            @RequestBody Map<String, Object> body,
            @RequestHeader Map<String, String> headers) {

        log.info("===== [WEBHOOK] Mercado Pago recebido =====");

        // Logs de segurança e auditoria
        log.info("HEADERS: {}", headers);
        log.info("PAYLOAD: {}", body);

        // Extrai alguns campos apenas para logging
        Object action = body.get("action");
        Object type = body.get("type");

        Map<String, Object> data = (body.get("data") instanceof Map)
                ? (Map<String, Object>) body.get("data")
                : null;

        String dataId = (data != null && data.get("id") != null)
                ? data.get("id").toString()
                : "Não informado";

        log.info("ACTION: {}", action);
        log.info("TYPE: {}", type);
        log.info("DATA.ID: {}", dataId);

        // Processamento real no service
        paymentWebhookService.processWebHook(body);

        log.info("===== [WEBHOOK] Processamento finalizado =====");

        return ResponseEntity.ok("Webhook recebido");
    }
}
