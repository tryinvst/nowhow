package com.project.billing.service;

import com.project.billing.dto.PaymentDto;
import com.project.billing.dto.PlanDto;
import com.project.billing.dto.PlanUpdateRequestDto;
import com.project.billing.dto.SubscriptionStatusDto;
import com.project.billing.entity.Payment;
import com.project.billing.entity.Plan;
import com.project.billing.entity.Subscription;
import com.project.billing.exception.ApiException;
import com.project.billing.repository.PaymentRepository;
import com.project.billing.repository.PlanRepository;
import com.project.billing.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Подписка на всю платформу: одна на пользователя, оплата продлевает её от текущей даты окончания.
 */
@Service
public class BillingService implements ApplicationRunner {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final boolean stubPayments;

    public BillingService(PlanRepository planRepository, SubscriptionRepository subscriptionRepository,
                          PaymentRepository paymentRepository,
                          @Value("${app.billing.stub-payments}") boolean stubPayments) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.stubPayments = stubPayments;
    }

    /**
     * Стартовые тарифы, если их ещё нет; цены потом меняет админ через PUT /plans/{code}.
     */
    @Override
    public void run(ApplicationArguments args) {
        if (planRepository.count() == 0) {
            planRepository.saveAll(List.of(
                    new Plan("MONTH", "Месяц", 99_000, 30),
                    new Plan("YEAR", "Год", 799_000, 365)));
        }
    }

    public List<PlanDto> activePlans() {
        return planRepository.findAllByActiveTrueOrderByPeriodDaysAsc().stream().map(PlanDto::from).toList();
    }

    public PlanDto updatePlan(String code, PlanUpdateRequestDto requestDto) {
        Plan plan = planRepository.findById(code)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Тариф " + code + " не найден"));
        plan.setTitle(requestDto.title().trim());
        plan.setPriceKopecks(requestDto.priceKopecks());
        plan.setActive(requestDto.active());
        return PlanDto.from(planRepository.save(plan));
    }

    /**
     * Заглушка оплаты: сразу «оплачено». С ЮKassa здесь будет создание платежа и редирект, а продление - в вебхуке.
     */
    @Transactional
    public SubscriptionStatusDto checkout(Long userId, String planCode) {
        if (!stubPayments) {
            throw new ApiException(HttpStatus.NOT_IMPLEMENTED, "Онлайн-оплата пока не подключена");
        }
        Plan plan = planRepository.findById(planCode)
                .filter(Plan::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Тариф " + planCode + " недоступен"));
        paymentRepository.save(new Payment(userId, plan.getCode(), plan.getPriceKopecks(), "stub", "PAID"));
        Subscription subscription = subscriptionRepository.findForUpdate(userId).orElse(new Subscription(userId));
        Instant now = Instant.now();
        if (!subscription.isActive()) {
            subscription.setStartedAt(now);
            subscription.setExpiresAt(now);
        }
        subscription.setPlanCode(plan.getCode());
        subscription.setExpiresAt(subscription.getExpiresAt().plus(Duration.ofDays(plan.getPeriodDays())));
        return toDto(subscriptionRepository.save(subscription));
    }

    public SubscriptionStatusDto status(Long userId) {
        return subscriptionRepository.findById(userId)
                .map(this::toDto)
                .orElse(new SubscriptionStatusDto(false, null, null, null));
    }

    public List<PaymentDto> payments(Long userId) {
        return paymentRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream().map(PaymentDto::from).toList();
    }

    private SubscriptionStatusDto toDto(Subscription subscription) {
        return new SubscriptionStatusDto(subscription.isActive(), subscription.getPlanCode(),
                subscription.getStartedAt(), subscription.getExpiresAt());
    }
}
