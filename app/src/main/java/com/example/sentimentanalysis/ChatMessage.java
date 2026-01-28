package com.example.sentimentanalysis;

public class ChatMessage {

    // ---------------- SENDER TYPES ----------------
    public static final int SENDER_USER = 0;
    public static final int SENDER_BOT = 1;
    public static final int SENDER_BOT_TYPING = 2; // New: typing indicator

    // ---------------- MESSAGE TYPES ----------------
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_PDF = 2;

    private final String text;         // For text or caption
    private final int senderType;      // User, Bot, or Typing
    private final int messageType;     // Text, Image, or PDF
    private final String filePath;     // Optional: local file path (image/pdf)
    private final long timestamp;      // Timestamp in millis

    // ---------------- CONSTRUCTORS ----------------
    // Text message with timestamp
    public ChatMessage(String text, int senderType, long timestamp) {
        this(text, senderType, TYPE_TEXT, null, timestamp);
    }

    // Full constructor
    public ChatMessage(String text, int senderType, int messageType, String filePath, long timestamp) {
        this.text = text;
        this.senderType = senderType;
        this.messageType = messageType;
        this.filePath = filePath;
        this.timestamp = timestamp;
    }

    // For backward compatibility
    public ChatMessage(String text, int senderType) {
        this(text, senderType, TYPE_TEXT, null, System.currentTimeMillis());
    }

    public ChatMessage(String text, int senderType, int messageType, String filePath) {
        this(text, senderType, messageType, filePath, System.currentTimeMillis());
    }

    // ---------------- GETTERS ----------------
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

    public long getTimestamp() {
        return timestamp;
    }
}