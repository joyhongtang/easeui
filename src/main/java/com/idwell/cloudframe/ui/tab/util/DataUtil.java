package com.idwell.cloudframe.ui.tab.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.blankj.utilcode.util.StringUtils.getString;

public class DataUtil {
    /**
     *    * yy/MM/dd HH:mm:ss 如 '2002/1/1 17:55:00'
     *    * yy/MM/dd HH:mm:ss pm 如 '2002/1/1 17:55:00 pm'
     *    * yy-MM-dd HH:mm:ss 如 '2002-1-1 17:55:00'
     *    * yy-MM-dd HH:mm:ss am 如 '2002-1-1 17:55:00 am' 
     * <p>
     * <p>
     * ————————————————
     * 版权声明：本文为CSDN博主「远经潮」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
     * 原文链接：https://blog.csdn.net/huanongjingchao/java/article/details/43499253
     *
     * @param pattern
     * @param dateTime
     * @return
     */
    public static String getFormatedDateTime(String pattern, long dateTime) {
        SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern);
        return sDateFormat.format(new Date(dateTime + 0));
    }

    /**
     * 将毫秒转换为年月日时分秒
     *
     * @author GaoHuanjie
     */
    public static String getYearMonthDayHourMinuteSecond(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        String mToMonth = null;
        if (String.valueOf(month).length() == 1) {
            mToMonth = "0" + month;
        } else {
            mToMonth = String.valueOf(month);
        }

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dToDay = null;
        if (String.valueOf(day).length() == 1) {
            dToDay = "0" + day;
        } else {
            dToDay = String.valueOf(day);
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String hToHour = null;
        if (String.valueOf(hour).length() == 1) {
            hToHour = "0" + hour;
        } else {
            hToHour = String.valueOf(hour);
        }

        int minute = calendar.get(Calendar.MINUTE);
        String mToMinute = null;
        if (String.valueOf(minute).length() == 1) {
            mToMinute = "0" + minute;
        } else {
            mToMinute = String.valueOf(minute);
        }

        int second = calendar.get(Calendar.SECOND);
        String sToSecond = null;
        if (String.valueOf(second).length() == 1) {
            sToSecond = "0" + second;
        } else {
            sToSecond = String.valueOf(second);
        }
        return year + "-" + mToMonth + "-" + dToDay + " " + hToHour + ":" + mToMinute + ":" + sToSecond;
    }

    public static String parseDuration(long ms) {
        //小于1小时
        if (ms < 1000 * 60 * 60) {
            return timeParse(ms);
        }
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String hms = formatter.format(ms);
        return hms;
    }

    public static String timeParse(long duration) {
        String time = "";

        long minute = duration / 60000;
        long seconds = duration % 60000;

        long second = Math.round((float) seconds / 1000);

        if (minute < 10) {
            time += "0";
        }
        time += minute + " : ";

        if (second < 10) {
            time += "0";
        }
        time += second;
        time += "";
        return time;
    }

}
