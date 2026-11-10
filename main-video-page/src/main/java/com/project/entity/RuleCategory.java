package com.project.entity;

/**
 * Разделы black-листа модерации.
 */
public enum RuleCategory {
    VIOLENCE("Призывы к агрессии и насилию"),
    SUICIDE("Призывы к самоубийству"),
    TERRORISM("Терроризм"),
    PORNOGRAPHY("Порнография"),
    POLITICS("Агитация: выборы, референдумы, волеизъявление");

    private final String title;

    RuleCategory(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
