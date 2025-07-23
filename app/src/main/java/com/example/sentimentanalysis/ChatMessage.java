package com.example.sentimentanalysis;

public class ChatMessage {
    public static final int SENDER_USER = 0;
    public static final int SENDER_BOT = 1;

    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_PDF = 2;

    private final String text;         // For text or caption
    private final int senderType;      // User or Bot
    private final int messageType;     // Text, Image, or PDF
    private final String filePath;     // Optional: local file path (image/pdf)

    public ChatMessage(String text, int senderType) {
        this(text, senderType, TYPE_TEXT, null);
    }

    public ChatMessage(String text, int senderType, int messageType, String filePath) {
        this.text = text;
        this.senderType = senderType;
        this.messageType = messageType;
        this.filePath = filePath;
    }

    public String getText() {
        return text;
    }

    public int getSenderType() {
        return senderType;
    }

    public int getMessageType() {
        return messageType;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isFileMessage() {
        return messageType == TYPE_IMAGE || messageType == TYPE_PDF;
    }
}