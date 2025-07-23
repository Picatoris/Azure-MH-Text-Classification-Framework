package com.example.sentimentanalysis;

public class DoctorModel {
    String name, description, languages, expertise, availability;
    int imageResId;

    public DoctorModel(String name, String description, String languages, String expertise, String availability, int imageResId) {
        this.name = name;
        this.description = description;
        this.languages = languages;
        this.expertise = expertise;
        this.availability = availability;
        this.imageResId = imageResId;
    }
}
