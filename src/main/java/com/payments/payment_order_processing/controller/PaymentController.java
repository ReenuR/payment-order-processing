package com.payments.payment_order_processing.controller;

import com.payments.payment_order_processing.dto.PaymentRequestDTO;
import com.payments.payment_order_processing.dto.PaymentResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.payments.payment_order_processing.service.PaymentService;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService){
        this.paymentService = paymentService;
    }
    @PostMapping("/orders/pay")
    public ResponseEntity<PaymentResponseDTO> processPayment(@RequestBody PaymentRequestDTO paymentRequestDTO){
        PaymentResponseDTO responseDTO= paymentService.processPayment(paymentRequestDTO);
        return ResponseEntity.accepted().body(responseDTO);
    }
}
