package com.sekoor.client.gui;

/**
 *
 */
public class Message {

    private String senderName;

    public Message(String senderName) {
        this.senderName = senderName;
    }

    public Message() {
        this.senderName = "";
    }


    @Override
    public String toString() {
        return "Message{" +
                "senderName='" + senderName + '\'' +
                '}';
    }
}
