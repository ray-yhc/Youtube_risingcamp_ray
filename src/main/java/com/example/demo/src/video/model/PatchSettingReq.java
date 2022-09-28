package com.example.demo.src.video.model;

import lombok.*;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor // 해당 클래스의 모든 멤버 변수(userIdx, nickname)를 받는 생성자를 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PatchSettingReq {
    private String title;
    private String thumbnailFileName;
    private String uploadedDate;
    private String description;
    private int languageId;
    private String shorts;
    private String primiered;
    private String streaming;
    private String streamStatus;
    private String kidContent;
    private String ageLimited;
    private String productPlacement;
    private String recordedDate;
    private String recordedLocation;
    private String licence;
    private String allowShare;
    private String alertToSubscribers;
    private String allowSamplingClip;
    private String category;
    private String allowComment;
    private String sortComment;
    private String showLike;
    private String accessStatus;
}
