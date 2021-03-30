package com.example.testfirebase.models;

public class Event {
    private String date, name;
    private long time;

    public Event(){

    }

    public Event(String date, String name, long time) {
        this.date = date;
        this.name = name;
        this.time= time;
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

    public long getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
