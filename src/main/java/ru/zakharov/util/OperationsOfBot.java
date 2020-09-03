package ru.zakharov.util;

public enum OperationsOfBot {
    CURRENT_DATE("текущая дата"),
    COUNT_OF_USERS_ON_SERVER("кол-во пользователей на сервере"),
    LIST_OF_USERS("список пользователей");

    private String description;

    OperationsOfBot(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
