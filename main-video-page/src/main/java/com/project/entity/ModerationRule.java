package com.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Правило black-листа: регулярное выражение по тексту в нижнем регистре, ё заменена на е.
 */
@Entity
@Table(name = "moderation_rules")
public class ModerationRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleCategory category;
    @Column(nullable = false, length = 1000)
    private String pattern;
    private String comment;

    public ModerationRule() {
    }

    public ModerationRule(RuleCategory category, String pattern, String comment) {
        this.category = category;
        this.pattern = pattern;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public RuleCategory getCategory() {
        return category;
    }

    public String getPattern() {
        return pattern;
    }

    public String getComment() {
        return comment;
    }
}
