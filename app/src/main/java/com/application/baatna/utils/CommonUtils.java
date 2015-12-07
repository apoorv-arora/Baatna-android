package com.application.baatna.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by yogeshmadaan on 01/12/15.
 */
public class CommonUtils {
    public static String findDateDifference(Long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm a");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm a");
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTimeInMillis(timestamp);
        calendar2.setTimeInMillis(System.currentTimeMillis());
        try {


            //in milliseconds
            long diff = calendar2.getTimeInMillis() - calendar1.getTimeInMillis();

            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);
            long diffYears = diffDays / 365;
//            System.out.print(diffDays + " days, ");
//            System.out.print(diffHours + " hours, ");
//            System.out.print(diffMinutes + " minutes, ");
//            System.out.print(diffSeconds + " seconds.");
//            Log.e("difference is", diffDays + " days, " + diffHours + " hours, " + diffMinutes + " minutes, ");
            if (diffDays == 0) {
                if (diffMinutes > 0) {
                    return diffMinutes + "MINS AGO";
                    //return timeFormat.format(calendar1.getTime());
                } else return "Just Now";
            } else if (diffDays == 1) {
                return "Yesterday";
            } else //if (diffDays = 30) // check if needed
             {
                return "" + diffDays + " DAYS AGO";
            } //else // unformated timestamp looks bad on ui
                // return format.format(calendar1.getTime());


        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }
}
