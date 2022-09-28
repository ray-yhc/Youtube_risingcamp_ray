package com.example.demo.src.channel;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.channel.model.GetChannelRes;
import com.example.demo.src.channel.model.GetVideoSummaryRes;
import com.example.demo.src.comment.model.GetCommentRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

@RestController // Rest API 또는 WebAPI를 개발하기 위한 어노테이션. @Controller + @ResponseBody 를 합친것.
// @Controller      [Presentation Layer에서 Contoller를 명시하기 위해 사용]
//  [Presentation Layer?] 클라이언트와 최초로 만나는 곳으로 데이터 입출력이 발생하는 곳
//  Web MVC 코드에 사용되는 어노테이션. @RequestMapping 어노테이션을 해당 어노테이션 밑에서만 사용할 수 있다.
// @ResponseBody    모든 method의 return object를 적절한 형태로 변환 후, HTTP Response Body에 담아 반환.
@RequestMapping("/app/channel")
// method가 어떤 HTTP 요청을 처리할 것인가를 작성한다.
// 요청에 대해 어떤 Controller, 어떤 메소드가 처리할지를 맵핑하기 위한 어노테이션
// URL(/app/users)을 컨트롤러의 메서드와 매핑할 때 사용
/**
 * Controller란?
 * 사용자의 Request를 전달받아 요청의 처리를 담당하는 Service, Prodiver 를 호출
 */
public class ChannelController {
    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************

    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기: 일단은 모르고 넘어가셔도 무방합니다.

    @Autowired  // 객체 생성을 스프링에서 자동으로 생성해주는 역할. 주입하려 하는 객체의 타입이 일치하는 객체를 자동으로 주입한다.
    // IoC(Inversion of Control, 제어의 역전) / DI(Dependency Injection, 의존관계 주입)에 대한 공부하시면, 더 깊이 있게 Spring에 대한 공부를 하실 수 있을 겁니다!(일단은 모르고 넘어가셔도 무방합니다.)
    // IoC 간단설명,  메소드나 객체의 호출작업을 개발자가 결정하는 것이 아니라, 외부에서 결정되는 것을 의미
    // DI 간단설명, 객체를 직접 생성하는 게 아니라 외부에서 생성한 후 주입 시켜주는 방식
    private final ChannelProvider channelProvider;
    @Autowired
    private final ChannelService channelService;
    @Autowired
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    public ChannelController(ChannelProvider channelProvider, ChannelService channelService, JwtService jwtService) {
        this.channelProvider = channelProvider;
        this.channelService = channelService;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }

    // ******************************************************************************

    /**
     * 채널 상단 정보 조회
     * [GET] /app/channel/{cid}
     */
    @ResponseBody
    @GetMapping("/{cid}")
    public BaseResponse<GetChannelRes> getChannelInfo(@PathVariable("cid") Long cid) {
        try {
            // jwt 에서 uid 추출
            long uid;
            try {
                uid = jwtService.getUserIdx();
                // uid 검증
                if (!channelProvider.verifyChannelId(uid))
                    throw new BaseException(INVALID_CHANNEL);
            } catch (BaseException exception) {
                uid = 0;
            }

            GetChannelRes getChannelRes = channelProvider.getChannelInfo(cid, uid);
            return new BaseResponse<>(getChannelRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        } catch (NumberFormatException exception) {
            logger.error(exception.getMessage());
            return new BaseResponse<>(REQUEST_ERROR);
        }
    }

    /**
     * 채널 동영상 리스트 조회
     * [GET] /{cid}/videos?p=
     */
    @ResponseBody
    @GetMapping("/{cid}/videos")
    public BaseResponse<List<GetVideoSummaryRes>> getVideosOfChannel(@PathVariable("cid") String cid,
                                                                     @RequestParam(required = false) Integer p) {
        try {
            // jwt 에서 uid 추출
            long uid;
            try {
                uid = jwtService.getUserIdx();
                // uid 검증
                if (!channelProvider.verifyChannelId(uid))
                    throw new BaseException(INVALID_CHANNEL);
            } catch (BaseException exception) {
                uid = 0;
            }

            if (p == null) p = 0;

            long parsed_cid = Long.parseLong(cid);

            return new BaseResponse<>(channelProvider.getVideoSummaryList(parsed_cid, uid, p));
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        } catch (NumberFormatException exception) {
            logger.error(exception.getMessage());
            return new BaseResponse<>(REQUEST_ERROR);
        }
    }

    /**
     * 채널 구독
     * [POST] /app/channel/{cid}/subscribe
     */
    @ResponseBody
    @PostMapping("/{cid}/subscribe")
    public BaseResponse<String> addSubscribeChannel(@PathVariable("cid") Long cid) {
        try {
            // jwt 에서 uid 추출
            long uid = jwtService.getUserIdx();
            // uid 검증
            if (!channelProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // cid 본인이라면 에러
            if (cid == uid)
                throw new BaseException(NOT_YOURSELF);
            // cid 검증
            if (!channelProvider.verifyChannelId(cid))
                throw new BaseException(INVALID_CHANNEL_ID);

            channelService.addSubscribeChannel(cid, uid);
            return new BaseResponse<>("");
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 채널 구독 취소
     * [PATCH] /app/channel/{cid}/subscribe
     */
    @ResponseBody
    @PatchMapping("/{cid}/subscribe")
    public BaseResponse<String> cancelSubscribeChannel(@PathVariable("cid") Long cid) {
        try {
            // jwt 에서 uid 추출
            long uid = jwtService.getUserIdx();
            // uid 검증
            if (!channelProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // cid 본인이라면 에러
            if (cid == uid)
                throw new BaseException(NOT_YOURSELF);
            // cid 검증
            if (!channelProvider.verifyChannelId(cid))
                throw new BaseException(INVALID_CHANNEL_ID);

            channelService.cancelSubscribeChannel(cid, uid);
            return new BaseResponse<>("");
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 채널 알림설정
     * [POST] /app/channel/{cid}/alert
     */
    @ResponseBody
    @PostMapping("/{cid}/alert")
    public BaseResponse<String> addAlertChannel(@PathVariable("cid") Long cid) {
        try {
            // jwt 에서 uid 추출
            long uid = jwtService.getUserIdx();
            // uid 검증
            if (!channelProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // cid 본인이라면 에러
            if (cid == uid)
                throw new BaseException(NOT_YOURSELF);
            // cid 검증
            if (!channelProvider.verifyChannelId(cid))
                throw new BaseException(INVALID_CHANNEL_ID);

            channelService.addAlertChannel(cid, uid);
            return new BaseResponse<>("");
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 채널 알림 취소
     * [PATCH] /app/channel/{cid}/alert
     */
    @ResponseBody
    @PatchMapping("/{cid}/alert")
    public BaseResponse<String> cancelAlertChannel(@PathVariable("cid") Long cid) {
        try {
            // jwt 에서 uid 추출
            long uid = jwtService.getUserIdx();
            // uid 검증
            if (!channelProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // cid 본인이라면 에러
            if (cid == uid)
                throw new BaseException(NOT_YOURSELF);
            // cid 검증
            if (!channelProvider.verifyChannelId(cid))
                throw new BaseException(INVALID_CHANNEL_ID);

            channelService.cancelAlertChannel(cid, uid);
            return new BaseResponse<>("");
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
