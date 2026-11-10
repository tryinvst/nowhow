package com.project.billing.controller;

import com.project.billing.dto.CheckoutRequestDto;
import com.project.billing.dto.PaymentDto;
import com.project.billing.dto.PlanDto;
import com.project.billing.dto.PlanUpdateRequestDto;
import com.project.billing.dto.SubscriptionStatusDto;
import com.project.billing.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "billing", description = "Тарифы и подписка NowHow")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/plans")
    @Operation(summary = "Тарифы", description = "Доступно без входа; цены в копейках")
    public List<PlanDto> plans() {
        return billingService.activePlans();
    }

    @PutMapping("/plans/{code}")
    @Operation(summary = "Изменить тариф", description = "Только админ")
    public PlanDto updatePlan(@PathVariable String code, @Valid @RequestBody PlanUpdateRequestDto requestDto) {
        return billingService.updatePlan(code, requestDto);
    }

    @PostMapping("/subscriptions/checkout")
    @Operation(summary = "Оформить подписку", description = "Сейчас заглушка: подписка активируется сразу, без оплаты. "
            + "Повторная оплата продлевает срок")
    public SubscriptionStatusDto checkout(@Valid @RequestBody CheckoutRequestDto requestDto,
                                          @AuthenticationPrincipal Jwt jwt) {
        return billingService.checkout(Long.valueOf(jwt.getSubject()), requestDto.planCode());
    }

    @GetMapping("/subscriptions/me")
    @Operation(summary = "Моя подписка")
    public SubscriptionStatusDto mySubscription(@AuthenticationPrincipal Jwt jwt) {
        return billingService.status(Long.valueOf(jwt.getSubject()));
    }

    @GetMapping("/payments/me")
    @Operation(summary = "Мои оплаты", description = "Последние 50")
    public List<PaymentDto> myPayments(@AuthenticationPrincipal Jwt jwt) {
        return billingService.payments(Long.valueOf(jwt.getSubject()));
    }
}
