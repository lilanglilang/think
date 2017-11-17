package com.inspur.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Time: 2017/11/13.
 */
public class TimeUtils {
    /**
     *
     * @return 数据格式:2014-12-19
     */
    public static String getCurDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }
    public static List<String> getBeenByDays(String start_time, String end_time){
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
        Date sdate=null;
        Date eDate=null;
        try {
            sdate=format.parse(start_time);
            eDate=format.parse(end_time);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        List<String> list=new ArrayList<String>();
        while (sdate.getTime()<=eDate.getTime()) {
            list.add(format.format(sdate));
            c.setTime(sdate);
            c.add(Calendar.DATE, 1); // 日期加1天
            sdate = c.getTime();
        }
        return list;
    }

    /**
     *
     * @return 数据格式:2014-12-19
     */

    public static String getCurDateTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(new Date());
    }
    /*
     * 将时间转换为时间戳
     * 时间格式为2017-01-01 11:12:13 否则抛异常
     */
    public static String dateToStamp(String s) throws ParseException{
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

    /**
     * 获取当前时间的时间戳
     * @return
     */
    public static String getCurDateStamp(){
       return String.valueOf(System.currentTimeMillis());
    }
    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }
}
