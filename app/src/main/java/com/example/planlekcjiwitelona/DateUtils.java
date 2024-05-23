package com.example.planlekcjiwitelona;

public class DateUtils {

    public static String getMonthname(int month) {
        String monthname;
        if (month == 1) {
            monthname = "Sty";
        } else if (month == 2) {
            monthname = "Lut";
        } else if (month == 3) {
            monthname = "Mar";
        } else if (month == 4) {
            monthname = "Kwi";
        } else if (month == 5) {
            monthname = "Maj";
        } else if (month == 6) {
            monthname = "Cze";
        } else if (month == 7) {
            monthname = "Lip";
        } else if (month == 8) {
            monthname = "Sie";
        } else if (month == 9) {
            monthname = "Wrz";
        } else if (month == 10) {
            monthname = "Paź";
        } else if (month == 11) {
            monthname = "Lis";
        } else if (month == 12) {
            monthname = "Gru";
        } else {
            monthname = "Wrng";
        }
        return monthname;
    }
}
