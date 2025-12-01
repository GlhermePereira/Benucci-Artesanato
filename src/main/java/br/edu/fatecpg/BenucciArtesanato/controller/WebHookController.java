package br.edu.fatecpg.BenucciArtesanato.controller;

import br.edu.fatecpg.BenucciArtesanato.service.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebHookController {

    private final PaymentWebhookService paymentWebhookService;

    @PostMapping("/mercadopago")
    public ResponseEntity<String> receiveMercadoPagoWebhook(
            @RequestBody Map<String, Object> body,
            @RequestHeader Map<String, String> headers) {

        log.info("===== WEBHOOK MERCADO PAGO RECEBIDO =====");
        log.info("HEADERS RECEBIDOS: {}", headers);
        log.info("PAYLOAD COMPLETO: {}", body);

        Object action = body.get("action");
        Object type = body.get("type");

        Map<String, Object> data = (body.get("data") instanceof Map)
                ? (Map<String, Object>) body.get("data")
                : null;

        String dataId = (data != null && data.get("id") != null)
                ? data.get("id").toString()
                : "Nao informado";

        log.info("ACTION: {}", action);
        log.info("TYPE: {}", type);
        log.info("DATA.ID: {}", dataId);

        // CHAMA O SERVICE REAL (agora corretamente)
        paymentWebhookService.processWebHook(body);

        log.info("===== FIM DO WEBHOOK =====");

        return ResponseEntity.ok("Webhook recebido");
    }


    @PostMapping("/mercadopago-teste")
    public ResponseEntity<String> receiveTestWebhook(
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestParam(value = "type", required = false) String type,
            @RequestBody(required = false) Map<String, Object> payload,
            @RequestHeader(value = "x-signature", required = false) String signature) {

        log.info("===== WEBHOOK TESTE RECEBIDO =====");
        log.info("data.id: {}", dataId);
        log.info("type: {}", type);
        log.info("signature: {}", signature);
        log.info("payload: {}", payload);

        return ResponseEntity.ok("Webhook de teste recebido");
    }
}
