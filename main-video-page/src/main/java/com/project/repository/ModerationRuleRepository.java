package com.project.repository;

import com.project.entity.ModerationRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModerationRuleRepository extends JpaRepository<ModerationRule, Long> {
}
