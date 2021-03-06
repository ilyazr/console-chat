package ru.zakharov.util;

public enum OperationsOfBot {
    CURRENT_DATE("текущая дата"),
    COUNT_OF_USERS_ON_SERVER("кол-во пользователей на сервере"),
    LIST_OF_USERS("список пользователей"),
    PRIVATE_MESSAGE("отправить личное сообщение"),
    CREATE_NEW_CHAT("создать приватный чат"),
    ALL_CHATS_ON_SERVER("показать все существующие чаты"),
    INVITE_USER_TO_THE_CHAT("пригласить пользователя в приватный чат"),
    CHECK_ALL_INVITES("посмотреть приглашения в приватные чаты");

    private String description;

    OperationsOfBot(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
