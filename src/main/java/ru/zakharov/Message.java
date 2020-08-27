package ru.zakharov;

import java.io.Serializable;

public class Message implements Serializable {

    private final String data;
    private final MessageType msgType;

    public Message(String data, MessageType msgType) {
        this.data = data;
        this.msgType = msgType;
    }

    public Message(MessageType msgType) {
        this.msgType = msgType;
        this.data = null;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public String getData() {
        return data;
    }
}
