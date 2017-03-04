package com.sounder.dcqvideo;/**
 * Created by Sounder on 2017/3/2.
 */

/**
 * Createed by Sounder on 2017/3/2
 */
public class Utils {
    private static StringBuilder sb = new StringBuilder();
    public static String parseTime(long mills){
        sb.setLength(0);
        long min = mills / 1000 /60;
        long sec = mills / 1000 % 60;
        sb.append(min < 10 ? "0" : "").append(String.valueOf(min))
                .append(":")
                .append(sec < 10 ? "0":"").append(String.valueOf(sec));
        return sb.toString();
    }
}
