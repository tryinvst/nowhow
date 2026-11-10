package com.project.billing.repository;

import com.project.billing.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, String> {
    List<Plan> findAllByActiveTrueOrderByPeriodDaysAsc();
}
