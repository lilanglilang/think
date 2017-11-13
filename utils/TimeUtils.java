package com.inspur.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Time: 2017/11/13.
 */
public class TimeUtils {
    public static String getCurrentTime(){
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        return  format.format(new Date());
    }
}
