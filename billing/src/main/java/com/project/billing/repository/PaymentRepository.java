package com.project.billing.repository;

import com.project.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);
}
