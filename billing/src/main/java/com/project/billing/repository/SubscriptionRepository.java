package com.project.billing.repository;

import com.project.billing.entity.Subscription;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    /**
     * Блокировка строки: два одновременных продления не потеряют оплаченные дни.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscription s where s.userId = :userId")
    Optional<Subscription> findForUpdate(Long userId);
}
