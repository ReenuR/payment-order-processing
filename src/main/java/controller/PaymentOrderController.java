package controller;

import dto.PaymentRequestDTO;
import dto.PaymentResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.PaymentOrderService;

@RestController
@RequestMapping("/api/v1")
public class PaymentOrderController {

    private final PaymentOrderService paymentOrderService;

    public PaymentOrderController(PaymentOrderService paymentOrderService){
        this.paymentOrderService = paymentOrderService;
    }
    @PostMapping("/orders/pay")
    public ResponseEntity<PaymentResponseDTO> processPayment(@RequestBody PaymentRequestDTO paymentRequestDTO){
        PaymentResponseDTO responseDTO= paymentOrderService.processPayment(paymentRequestDTO);
        return ResponseEntity.accepted().body(responseDTO);
    }
}
