package com.example;

/**
 * Заготовка IntelliJ IDEA, созданная вместе с модулем: печатает приветствие и счётчик.
 * Общий код модуля лежит в пакете {@code com.project.common}.
 */
public class Main {

    /**
     * @param args аргументы командной строки, не используются
     */
    public static void main(String[] args) {
        System.out.printf ( "Hello and welcome!" );

        for (int i = 1; i <= 5; i++) {
            System.out.println ( "i = " + i );
        }
    }
}
