package com.example.demo.src.video.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor // 해당 클래스의 모든 멤버 변수(userIdx, nickname)를 받는 생성자를 생성
public class GetSettingRes {
    private long videoId;
    private String title;
    private long channelId;
    private String thumbnailImg1xUrl;
    private String videoLength;
    private String uploadedDate;
    private int maxResolution;
    private String shareUrl;
    private String videoUrl;
    private String description;
    private long languageId;
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
