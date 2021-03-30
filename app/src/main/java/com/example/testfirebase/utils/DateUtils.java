package com.example.testfirebase.utils;

import com.example.testfirebase.R;
import com.example.testfirebase.models.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtils {

    public static SimpleDateFormat dateFormat= new SimpleDateFormat("dd/MM/yyyy");

    public static boolean checkDate(int year, int month, int day){
        Calendar pickedCalendar= Calendar.getInstance();
        pickedCalendar.set(year, month, day);
        Calendar today= Calendar.getInstance();
        if(today.getTimeInMillis()-pickedCalendar.getTimeInMillis()<0){
            return false;
        }
        return true;
    }

    public static int setHoroscope(String date) {
        String[] time= date.split("/");
        int day= Integer.parseInt(time[0]);
        int month= Integer.parseInt(time[1]);
        switch (month) {
            case 1:
                if (day < 20) {
                    return R.string.horoscope_capricorn;
                } else {
                    return R.string.horoscope_aquarius;
                }
            case 2:
                if (day < 19) {
                    return R.string.horoscope_aquarius;
                } else {
                    return R.string.horoscope_pisces;
                }
            case 3:
                if (day < 20) {
                    return R.string.horoscope_pisces;
                } else {
                    return R.string.horoscope_aries;
                }
            case 4:
                if (day < 20) {
                    return R.string.horoscope_aries;
                } else {
                    return R.string.horoscope_taurus;
                }
            case 5:
                if (day < 21) {
                    return R.string.horoscope_taurus;
                } else {
                    return R.string.horoscope_gemini;
                }
            case 6:
                if (day < 21) {
                    return R.string.horoscope_gemini;
                } else {
                    return R.string.horoscope_cancer;
                }
            case 7:
                if (day < 23) {
                    return R.string.horoscope_cancer;
                } else {
                    return R.string.horoscope_leo;
                }
            case 8:
                if (day < 23) {
                    return R.string.horoscope_leo;
                } else {
                    return R.string.horoscope_virgo;
                }
            case 9:
                if (day < 23) {
                    return R.string.horoscope_virgo;
                } else {
                    return R.string.horoscope_libra;
                }
            case 10:
                if (day < 23) {
                    return R.string.horoscope_libra;
                } else {
                    return R.string.horoscope_scorpio;
                }
            case 11:
                if (day < 22) {
                    return R.string.horoscope_scorpio;
                } else {
                    return R.string.horoscope_sagittarius;
                }
            case 12:
                if (day < 22) {
                    return R.string.horoscope_sagittarius;
                } else {
                    return R.string.horoscope_capricorn;
                }
        }
        return 0;
    }

    public static long getTime(String date){
        int year= Calendar.getInstance().get(Calendar.YEAR);
        String[] dates= date.split("/");
        Calendar eventTime= Calendar.getInstance();
        int day= Integer.parseInt(dates[0]);
        int month= Integer.parseInt(dates[1]);
        eventTime.set(year, month, day);
        return eventTime.getTimeInMillis();
    }

    public static boolean isUpcoming(String date){
        int year= Calendar.getInstance().get(Calendar.YEAR);
        String[] dates= date.split("/");
        Calendar eventTime= Calendar.getInstance();
        int day= Integer.parseInt(dates[0]);
        int month= Integer.parseInt(dates[1]);
        eventTime.set(year, month, day);
        if(eventTime.getTimeInMillis()- Calendar.getInstance().getTimeInMillis()>0 ){
            long toCome= (eventTime.getTimeInMillis()- Calendar.getInstance().getTimeInMillis())/1000;
            if(toCome<86400*7){
                return true;
            }
        }
        return false;
    }

}
