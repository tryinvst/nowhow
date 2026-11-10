package com.project.common;

/**
 * Единый справочник профессий: специализация мастера, категория видео и курса.
 * <p>Новые значения добавлять только в конец, коды не переименовывать - они хранятся в БД строкой.
 */
public enum Category {
    ELECTRICS("Электрика"),
    PLUMBING("Сантехника"),
    TILING("Плитка"),
    FINISHING("Отделка и ремонт"),
    CARPENTRY("Столярное дело"),
    WELDING("Сварка"),
    AUTO_REPAIR("Авторемонт"),
    PHOTO("Фотография"),
    VIDEO("Видеосъёмка"),
    BEAUTY("Красота"),
    COOKING("Кулинария"),
    GARDEN("Сад и огород"),
    OTHER("Другое");

    private final String title;

    Category(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
