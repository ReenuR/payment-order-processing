package com.payments.payment_order_processing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class PaymentOrderProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentOrderProcessingApplication.class, args);
	}

}
