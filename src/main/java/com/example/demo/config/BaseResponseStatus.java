package com.example.demo.config;

import lombok.Getter;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {
    /**
     * 1000 : 요청 성공
     */
    SUCCESS(true, 1000, "요청에 성공하였습니다."),


    /**
     * 2000 : Request 오류
     */
    // Common
    REQUEST_ERROR(false, 2000, "입력값을 확인해주세요."),
    EMPTY_JWT(false, 2001, "JWT를 입력해주세요."),
    INVALID_JWT(false, 2002, "유효하지 않은 JWT입니다."),
    INVALID_USER_JWT(false, 2003, "권한이 없는 유저의 접근입니다."),
    INAVLID_TIME_FORMAT(false, 2007, "형식이 올바르지 않습니다. (hh:mm:ss)"),
    INAVLID_DATE_FORMAT(false, 2008, "형식이 올바르지 않습니다. (yy-mm-dd)"),

    // users
    USERS_EMPTY_USER_ID(false, 2010, "유저 아이디 값을 확인해주세요."),

    // [POST] /users
    POST_USERS_EMPTY_EMAIL(false, 2015, "이메일을 입력해주세요."),
    POST_USERS_INVALID_EMAIL(false, 2016, "이메일 형식을 확인해주세요."),
    POST_USERS_EXISTS_EMAIL(false, 2017, "중복된 이메일입니다."),

    // 2100 : video

    // [POST] /app/video
    INAVLID_STARTVIEWPOINT_FORMAT(false, 2101, "startViewPoint의 형식이 올바르지 않습니다. (hh:mm:ss)"),
    INAVLID_ENDVIEWPOINT_FORMAT(false, 2102, "endViewPoint의 형식이 올바르지 않습니다. (hh:mm:ss)"),

    // [GET, POST, PATCH] /app/video/setting
    TOO_LONG_TITLE(false, 2110, "title 의 길이가 100자를 초과하였습니다."),
    TOO_LONG_THUMBNAILFILENAME(false, 2111, "thumbnailFileName의 길이가 100자를 초과하였습니다."),
    INVALID_VIDEOLENGTH_FORMAT(false, 2112, "videoLength 형식이 올바르지 않습니다. (hh:mm:ss)"),
    INVALID_UPLOADED_FORMAT(false, 2113, "uploadedDate 형식이 올바르지 않습니다. (yy-mm-dd)"),
    TOO_LONG_VIDEOFILENAME(false, 2114, "videoFileName의 길이가 100자를 초과하였습니다."),
    TOO_LONG_DESCRIPTION(false, 2115, "description의 길이가 1000자를 초과하였습니다."),
    INVALID_SHORTS(false, 2116, "shorts 데이터가 올바르지 않습니다."),
    INVALID_PRIMIERED(false, 2117, "primiered의 데이터가 올바르지 않습니다."),
    INVALID_STREAMING(false, 2118, "streaming의 데이터가 올바르지 않습니다."),
    INVALID_STREAMSTATUS(false, 2119, "streamStatus의 데이터가 올바르지 않습니다."),
    INVALID_KIDCONTENTS(false, 2120, "kidContent의 데이터가 올바르지 않습니다."),
    INVALID_AGELIMITED(false, 2121, "ageLimited의 데이터가 올바르지 않습니다."),
    INVALID_PPL(false, 2122, "productPlacement의 데이터가 올바르지 않습니다."),
    INVALID_RECORDED_FORMAT(false, 2123, "recordedDate의  형식이 올바르지 않습니다. (yy-mm-dd)"),
    TOO_LONG_LOCATION(false, 2124, "recordedLocation의 데이터가 100자를 초과했습니다."),
    INVALID_LICENCE(false, 2125, "licence의 데이터가 올바르지 않습니다."),
    INVALID_ALLOWSHARE(false, 2126, "allowShare의 데이터가 올바르지 않습니다."),
    INVALID_ALERT(false, 2127, "alertToSubscribers의 데이터가 올바르지 않습니다."),
    INVALID_ALLOWCLIP(false, 2128, "allowSamplingClip의 데이터가 올바르지 않습니다."),
    INVALID_CATEGORY(false, 2129, "category의 데이터가 올바르지 않습니다."),
    INVALID_ALLOWCOMMENT(false, 2030, "allowComment의 데이터가 올바르지 않습니다."),
    INVALID_SORTCOMMENT(false, 2131, "sortComment의 데이터가 올바르지 않습니다."),
    INVALID_SHOWLIKE(false, 2132, "showLike의 데이터가 올바르지 않습니다."),
    INVALID_ACCESS(false, 2133, "accessStatus의 데이터가 올바르지 않습니다."),

    // 2200 : Channel
    NOT_YOURSELF(false, 2200, "자신의 채널은 구독할 수 없습니다."),

    // 2300 : Comment
    EMPTY_COMMENT_QUERY_PARAMS(false, 2301, "vid 또는 commentid를 입력해주세요."),


    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),
    INVALID_CHANNEL(false, 3001, "존재하지 않는 사용자 채널 id입니다."),
    INVALID_CHANNEL_ID(false, 3002, "존재하지 않는 채널 id (cid) 입니다."),
    INVALID_VIDEO_ID(false, 3003, "존재하지 않는 영상 id (vid) 입니다."),
    INACCESSIBLE_VIDEO(false, 3003, "해당 영상에 접속 불가능한 사용자 채널입니다."),

    // [POST] /users
    DUPLICATED_EMAIL(false, 3013, "중복된 이메일입니다."),
    FAILED_TO_LOGIN(false, 3014, "없는 아이디거나 비밀번호가 틀렸습니다."),


    // 3100 : video
    // 3110 : 시청
    INVALID_VIEWVIDEO_ID(false, 3110, "존재하지 않는 View Id (viewId) 입니다."),

    // 3120 : 게시,수정,삭제
    // [GET, POST, PATCH] /app/video/setting
    INVALID_LANGUAGE_ID(false, 3030, "languageId가 존재하지 않는 언어 식별자입니다."),

    // 3130 : 좋아요, 싫어요
    EXISTING_DATA(false, 3030, "이미 존재하는 데이터입니다."),
    NON_EXISTING_DATA(false, 3031, "데이터가 존재하지 않습니다."),

    // 3200 : Channel

    // 3300 : Comment
    INVALID_COMMENT_ID(false, 3301, "존재하지 않는 Comment Id (commentid) 입니다."),


    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(false, 4000, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, 4001, "서버와의 연결에 실패하였습니다."),

    //[PATCH] /users/{userIdx}
    MODIFY_FAIL_USERNAME(false, 4014, "유저네임 수정 실패"),

    PASSWORD_ENCRYPTION_ERROR(false, 4011, "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR(false, 4012, "비밀번호 복호화에 실패하였습니다.");


    // 5000 : 필요시 만들어서 쓰세요
    // 6000 : 필요시 만들어서 쓰세요


    private final boolean isSuccess;
    private final int code;
    private final String message;

    private BaseResponseStatus(boolean isSuccess, int code, String message) { //BaseResponseStatus 에서 각 해당하는 코드를 생성자로 맵핑
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
