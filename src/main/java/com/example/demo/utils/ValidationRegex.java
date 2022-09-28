package com.example.demo.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationRegex {
    // 이메일 형식 체크
    public static boolean isRegexEmail(String target) {
        String regex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.find();
    }

    // 날짜 형식, 전화 번호 형식 등 여러 Regex 인터넷에 검색하면 나옴.


    // 시간 형식 체크 hh:mm:ss
    private static boolean isRegexTime(String checkDate) {
        String pattern = "(0[0-9]|1[0-9]|2[0-4]):[0-5][0-9]:[0-5][0-9]";
        return checkDate.matches(pattern);
    }

    // 날짜 형식 체크 yy:mm:dd
    private static boolean isRegexDate(String checkDate) {
        String pattern = "\\d{2}-" +
                // 1~31일
                "((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))" +
                // 1~30일
                "|((0[469]|11)-(0[1-9]|[12][0-9]|3[0]))" +
                // 2월 1~29일
                "|((02)-(0[1-9]|1[0-9]|2[0-9]))";
        return checkDate.matches(pattern);
    }
}

