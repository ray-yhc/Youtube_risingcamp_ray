package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 스프링 부트 테스트
 */
@SpringBootTest   // 통합 테스트, 스프링부트 어플리케이션 테스트에 필요한 거의 모든 의존성을 제공.
class DemoApplicationTests {
//
//    @Test
//    void contextLoads() {
//    }
//


    @Test
    public void hashTagTest() {
        String title = "새로운 영상 #테스트";
        String des = "#RC #라이징캠프 샘플 영상 설명입니다";
        List<String> stList = extractHashTag(title, des);

        System.out.println(stList);
    }

    @Test
    public List<String> extractHashTag(String title, String des) {

        Pattern p = Pattern.compile("\\#([0-9a-zA-Z가-힣]*)");
        List<String> resultList = new ArrayList<>();

        // 제목 추적
        Matcher m = p.matcher(title);
        String extractHashTag = null;
        while (m.find()) {
            extractHashTag = sepcialCharacter_replace(m.group());

            if (extractHashTag != null)
                resultList.add(extractHashTag);
        }

        // 본문 추적
        m = p.matcher(des);
        while (m.find()) {
            extractHashTag = sepcialCharacter_replace(m.group());

            if (extractHashTag != null)
                resultList.add(extractHashTag);
        }

        return resultList;
    }

    public String sepcialCharacter_replace(String str) {
        String result = str.replaceAll("[-_+=!@#[$]%\\^&[*]\\(\\)\\[\\]\\{\\}[|];:'\"<>,.?/~`）//r]", "");

        if (result.length() < 1) {
            return null;
        }

        return result;
    }


}