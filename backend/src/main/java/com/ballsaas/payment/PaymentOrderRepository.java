package com.ballsaas.payment;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByPaymentNo(String paymentNo);

    Optional<PaymentOrder> findFirstByBusinessTypeAndBusinessIdOrderByCreatedAtDesc(BusinessType businessType, Long businessId);
}
