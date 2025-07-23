package com.example.sentimentanalysis;

public class HelplineModel {
    private String title;
    private String description;
    private String phone;
    private String availability;

    public HelplineModel(String title, String description, String phone, String availability) {
        this.title = title;
        this.description = description;
        this.phone = phone;
        this.availability = availability;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvailability() {
        return availability;
    }
}
