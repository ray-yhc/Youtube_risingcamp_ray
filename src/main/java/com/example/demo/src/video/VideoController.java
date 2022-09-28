package com.example.demo.src.video;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.utils.Verifier;
import com.example.demo.src.video.model.*;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.config.BaseResponseStatus.*;

@RestController // Rest API 또는 WebAPI를 개발하기 위한 어노테이션. @Controller + @ResponseBody 를 합친것.
// @Controller      [Presentation Layer에서 Contoller를 명시하기 위해 사용]
//  [Presentation Layer?] 클라이언트와 최초로 만나는 곳으로 데이터 입출력이 발생하는 곳
//  Web MVC 코드에 사용되는 어노테이션. @RequestMapping 어노테이션을 해당 어노테이션 밑에서만 사용할 수 있다.
// @ResponseBody    모든 method의 return object를 적절한 형태로 변환 후, HTTP Response Body에 담아 반환.
@RequestMapping("/app/video")
// method가 어떤 HTTP 요청을 처리할 것인가를 작성한다.
// 요청에 대해 어떤 Controller, 어떤 메소드가 처리할지를 맵핑하기 위한 어노테이션
// URL(/app/users)을 컨트롤러의 메서드와 매핑할 때 사용
/**
 * Controller란?
 * 사용자의 Request를 전달받아 요청의 처리를 담당하는 Service, Prodiver 를 호출
 */
public class VideoController {
    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************

    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기: 일단은 모르고 넘어가셔도 무방합니다.

    @Autowired  // 객체 생성을 스프링에서 자동으로 생성해주는 역할. 주입하려 하는 객체의 타입이 일치하는 객체를 자동으로 주입한다.
    // IoC(Inversion of Control, 제어의 역전) / DI(Dependency Injection, 의존관계 주입)에 대한 공부하시면, 더 깊이 있게 Spring에 대한 공부를 하실 수 있을 겁니다!(일단은 모르고 넘어가셔도 무방합니다.)
    // IoC 간단설명,  메소드나 객체의 호출작업을 개발자가 결정하는 것이 아니라, 외부에서 결정되는 것을 의미
    // DI 간단설명, 객체를 직접 생성하는 게 아니라 외부에서 생성한 후 주입 시켜주는 방식
    private final VideoProvider videoProvider;
    @Autowired
    private final VideoService videoService;
    @Autowired
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    public VideoController(VideoProvider videoProvider, VideoService videoService, JwtService jwtService) {
        this.videoProvider = videoProvider;
        this.videoService = videoService;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }

    // ******************************************************************************
    private Verifier verifier;
    @Autowired
    public void setVerifier(Verifier verifier){
        this.verifier = verifier;
    }

    /**
     * 포맷 검증 메소드
     */
    private static boolean checkTime(String checkDate) {
        String pattern = "(0[0-9]|1[0-9]|2[0-4]):[0-5][0-9]:[0-5][0-9]";
        return checkDate.matches(pattern);
    }
    private static boolean checkDate(String checkDate) {
        String pattern = "\\d{2}-" +
                // 1~31일
                "((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))" +
                // 1~30일
                "|((0[469]|11)-(0[1-9]|[12][0-9]|3[0]))" +
                // 2월 1~29일
                "|((02)-(0[1-9]|1[0-9]|2[0-9]))";
        return checkDate.matches(pattern);
    }
    // ******************************************************************************

    /**
     * 영상 시청 시 필요한 정보 호출
     * [GET] /app/video
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<GetVideoRes> getVideoInfo(@RequestParam() Long vid) {
        try {
            GetVideoRes getVideoRes = videoProvider.getVideoInfoById(vid);
            return new BaseResponse<>(getVideoRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 영상시청 시작 -> 조회수 기록
     * [POST] /app/video
     */
    @ResponseBody
    @PostMapping("")    // POST 방식의 요청을 매핑하기 위한 어노테이션
    public BaseResponse<PostViewStartRes> viewStart(@RequestBody PostViewStartReq postViewStartReq,
                                                    @RequestParam() Long vid) {
        try {
            // jwt 에서 uid 추출
            long uid;
            uid = jwtService.getUserIdx();
            // uid 검증
            if (!videoProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // vid 검증
            if (!videoProvider.verifyVideoId(vid))
                throw new BaseException(INVALID_VIDEO_ID);
            // 시간 형식 검증
            if (!checkTime(postViewStartReq.getStartViewPoint()))
                throw new BaseException(INAVLID_STARTVIEWPOINT_FORMAT);

            long viewId = videoService.viewStart(vid, uid, postViewStartReq);
            return new BaseResponse<>(new PostViewStartRes(viewId));
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    /**
     * 영상시청 종료 -> 조회수, 시청시간 기록
     * [PATCH] /app/video
     */
    @ResponseBody
    @PatchMapping("")
    public BaseResponse<String> viewEnd(@RequestBody PatchViewEndReq patchViewEndReq,
                                        @RequestParam() Long vid) {
        try {
            // jwt 에서 uid 추출
            long uid;
            uid = jwtService.getUserIdx();
            // uid 검증
            if (!videoProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // vid 검증
            if (!videoProvider.verifyVideoId(vid))
                throw new BaseException(INVALID_VIDEO_ID);
            // viewId 검증
            if (!videoProvider.verifyViewId(patchViewEndReq.getViewId()))
                throw new BaseException(INVALID_VIEWVIDEO_ID);
            // 시간 형식 검증
            if (!checkTime(patchViewEndReq.getEndViewPoint()))
                throw new BaseException(INAVLID_ENDVIEWPOINT_FORMAT);

            videoService.viewEnd(vid, uid, patchViewEndReq);
            return new BaseResponse<>("success");
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 새로운 영상 업로드
     * [POST] /app/video/setting
     */
    @ResponseBody
    @PostMapping("/setting")    // POST 방식의 요청을 매핑하기 위한 어노테이션
    public BaseResponse<PostSettingRes> createUser(@RequestBody PostSettingReq postSettingReq) {
        //  @RequestBody란, 클라이언트가 전송하는 HTTP Request Body(우리는 JSON으로 통신하니, 이 경우 body는 JSON)를 자바 객체로 매핑시켜주는 어노테이션

        try {
            // jwt 에서 uid 추출
            long uid;
            uid = jwtService.getUserIdx();
            // uid 검증
            if (!videoProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);

            PostSettingRes postSettingRes = videoService.uploadNewVideo(postSettingReq, uid);
            return new BaseResponse<>(postSettingRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 영상 세부정보 조회
     * [GET] /app/video/setting?vid
     */
    @ResponseBody
    @GetMapping("/setting")
    public BaseResponse<GetSettingRes> getVideoSettingInfo(@RequestParam() Long vid) {
        try {
            // jwt 에서 uid 추출
            long uid;
            uid = jwtService.getUserIdx();
            // uid 검증
            if (!verifier.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // vid 검증
            if (!verifier.verifyVideoId(vid))
                throw new BaseException(INVALID_VIDEO_ID);
            // 접속가능한 uid인지 확인
            if (!verifier.authorizeVideo(vid,uid))
                throw new BaseException(INACCESSIBLE_VIDEO);

            return new BaseResponse<>(videoProvider.getVideoSettingInfo(vid));
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    /**
     * 영상 세부정보 수정
     * [PATCH] /app/video/setting?vid
     */
    @ResponseBody
    @PatchMapping("/setting")
    public BaseResponse<String> updateVideoSettingInfo(@RequestBody PatchSettingReq patchSettingReq, @RequestParam() Long vid) {
        try {
            // jwt 에서 uid 추출
            long uid;
            uid = jwtService.getUserIdx();
            // uid 검증
            if (!videoProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // vid 검증
            if (!videoProvider.verifyVideoId(vid))
                throw new BaseException(INVALID_VIDEO_ID);
            // 접속가능한 uid인지 확인
            if (!videoProvider.authorizeVideo(vid,uid))
                throw new BaseException(INACCESSIBLE_VIDEO);

            if (videoService.updateVideoSettingInfo(patchSettingReq, vid) == 1)
                return new BaseResponse<>("Success");
            else return new BaseResponse<>(REQUEST_ERROR);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 영상 삭제
     * [PATCH] /app/video/d?vid=
     */
    @ResponseBody
    @PatchMapping("/d")
    public BaseResponse<String> deleteVideo(@RequestParam() long vid) {
        try {
            // jwt 에서 uid 추출
            long uid;
            uid = jwtService.getUserIdx();
            // uid 검증
            if (!videoProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // vid 검증
            if (!videoProvider.verifyVideoId(vid))
                throw new BaseException(INVALID_VIDEO_ID);
            // 접속가능한 uid인지 확인
            if (!videoProvider.authorizeVideo(vid,uid))
                throw new BaseException(INACCESSIBLE_VIDEO);

            if (videoService.deleteVideo(vid) > 0)
                return new BaseResponse<>("Successfully deleted");
            else return new BaseResponse<>(REQUEST_ERROR);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 영상 좋아요 누르기
     * [POST] /app/video/like?vid=
     */
    @ResponseBody
    @PostMapping("/like")
    public BaseResponse<String> addLikeVideo(@RequestParam() long vid) {
        try {
            // jwt 에서 uid 추출
            long uid = jwtService.getUserIdx();
            // uid 검증
            if (!videoProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // vid 검증
            if (!videoProvider.verifyVideoId(vid))
                throw new BaseException(INVALID_VIDEO_ID);

            // cid,vid 이용해 데이터 추가
            videoService.addLikeVideo(vid, uid);
            return new BaseResponse<>("");
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    /**
     * 영상 좋아요 취소
     * [PATCH] /app/video/like?vid=
     */
    @ResponseBody
    @PatchMapping("/like")
    public BaseResponse<String> cancelLikeVideo(@RequestParam() long vid) {
        try {
            // jwt 에서 uid 추출
            long uid = jwtService.getUserIdx();
            // uid 검증
            if (!videoProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // vid 검증
            if (!videoProvider.verifyVideoId(vid))
                throw new BaseException(INVALID_VIDEO_ID);

            // cid,vid 이용해 데이터 삭제
            videoService.cancelLikeVideo(vid, uid);
            return new BaseResponse<>("");
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     *  영상 싫어요 누르기
     *  [POST] /app/video/dislike?vid=
     */
    @ResponseBody
    @PostMapping("/dislike")
    public BaseResponse<String> addDislikeVideo(@RequestParam() long vid) {
        try {
            // jwt 에서 cid 추출
            long uid = jwtService.getUserIdx();
            // uid 검증
            if (!videoProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // vid 검증
            if (!videoProvider.verifyVideoId(vid))
                throw new BaseException(INVALID_VIDEO_ID);

            videoService.addDislikeVideo(vid,uid);
            return new BaseResponse<>("");
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }


    /**
     *  영상 싫아요 취소
     *  [PATCH] /app/video/dislike?vid=
     */
    @ResponseBody
    @PatchMapping("/dislike")
    public BaseResponse<String> cancelDislikeVideo(@RequestParam() long vid) {
        try {
            // jwt 에서 uid 추출
            long uid = jwtService.getUserIdx();
            // uid 검증
            if (!videoProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // vid 검증
            if (!videoProvider.verifyVideoId(vid))
                throw new BaseException(INVALID_VIDEO_ID);

            videoService.cancelDislikeVideo(vid,uid);
            return new BaseResponse<>("");
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }
}

