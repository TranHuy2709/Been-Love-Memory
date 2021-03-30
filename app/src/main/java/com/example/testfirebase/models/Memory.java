package com.example.testfirebase.models;

public class Memory {
    private String date, name;
    private int imageId;

    Memory(){

    }

    public Memory(String date, String name, int imageId) {
        this.date = date;
        this.name = name;
        this.imageId = imageId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
