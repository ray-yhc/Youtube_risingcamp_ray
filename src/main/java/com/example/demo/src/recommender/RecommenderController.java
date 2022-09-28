package com.example.demo.src.recommender;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.channel.model.GetVideoSummaryRes;
import com.example.demo.src.recommender.model.GetVideoInfoRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.INVALID_CHANNEL;
import static com.example.demo.config.BaseResponseStatus.REQUEST_ERROR;

@RestController // Rest API 또는 WebAPI를 개발하기 위한 어노테이션. @Controller + @ResponseBody 를 합친것.
// @Controller      [Presentation Layer에서 Contoller를 명시하기 위해 사용]
//  [Presentation Layer?] 클라이언트와 최초로 만나는 곳으로 데이터 입출력이 발생하는 곳
//  Web MVC 코드에 사용되는 어노테이션. @RequestMapping 어노테이션을 해당 어노테이션 밑에서만 사용할 수 있다.
// @ResponseBody    모든 method의 return object를 적절한 형태로 변환 후, HTTP Response Body에 담아 반환.
@RequestMapping("/app/recommender")
// method가 어떤 HTTP 요청을 처리할 것인가를 작성한다.
// 요청에 대해 어떤 Controller, 어떤 메소드가 처리할지를 맵핑하기 위한 어노테이션
// URL(/app/users)을 컨트롤러의 메서드와 매핑할 때 사용
/**
 * Controller란?
 * 사용자의 Request를 전달받아 요청의 처리를 담당하는 Service, Prodiver 를 호출
 */
public class RecommenderController {
    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************

    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기: 일단은 모르고 넘어가셔도 무방합니다.

    @Autowired  // 객체 생성을 스프링에서 자동으로 생성해주는 역할. 주입하려 하는 객체의 타입이 일치하는 객체를 자동으로 주입한다.
    // IoC(Inversion of Control, 제어의 역전) / DI(Dependency Injection, 의존관계 주입)에 대한 공부하시면, 더 깊이 있게 Spring에 대한 공부를 하실 수 있을 겁니다!(일단은 모르고 넘어가셔도 무방합니다.)
    // IoC 간단설명,  메소드나 객체의 호출작업을 개발자가 결정하는 것이 아니라, 외부에서 결정되는 것을 의미
    // DI 간단설명, 객체를 직접 생성하는 게 아니라 외부에서 생성한 후 주입 시켜주는 방식
    private final RecommenderProvider recommenderProvider;
    @Autowired
    private final RecommenderService recommenderService;
    @Autowired
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    public RecommenderController(RecommenderProvider recommenderProvider, RecommenderService recommenderService, JwtService jwtService) {
        this.recommenderProvider = recommenderProvider;
        this.recommenderService = recommenderService;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }

    // ******************************************************************************

//    /**
//     * 관련 영상 리스트 조회 (20개씩)
//     * [GET] /app/recommender?vid= & ?p=
//     */
//    @ResponseBody
//    @GetMapping("")
//    public BaseResponse<List<GetVideoInfoRes>> videoRecommender(@RequestParam long vid,
//                                                                @RequestParam(required = false) int p) {
//
//    }


    /**
     * 추천 영상 리스트 조회 (20개씩)
     * [GET] /app/recommender/main?p=
     */
    @ResponseBody
    @GetMapping("/main")
    public BaseResponse<List<GetVideoInfoRes>> mainRecommender(
            @RequestParam(required = false) Integer p) {
        try {
            if (p == null) p = 0;

            // jwt 에서 uid 추출
            long uid;
            try {
                uid = jwtService.getUserIdx();
                // uid 검증
                if (!recommenderProvider.verifyChannelId(uid))
                    throw new BaseException(INVALID_CHANNEL);
                // uid 있을 경우
                return new BaseResponse<>(recommenderProvider.mainRecommender(uid, p));
            } catch (BaseException exception) {
                // uid 없을 경우
                return new BaseResponse<>(recommenderProvider.mainRecommender(p));
            }
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        } catch (NumberFormatException exception) {
            logger.error(exception.getMessage());
            return new BaseResponse<>(REQUEST_ERROR);
        }
    }


    /**
     * 추천 쇼츠 리스트 조회 (8개씩)
     * [GET] /app/recommender/shorts
     */


//    /**
//     * 채널 동영상 리스트 조회
//     * [GET] /{cid}/videos?cid=&vid=&p=
//     */
//    @ResponseBody
//    @GetMapping("/{cid}/videos")
//    public BaseResponse<List<GetVideoSummaryRes>> getVideosOfChannel(@PathVariable("cid") String cid,
//                                                                     @RequestParam(required = false) String p) {
//        try {
//            // jwt 에서 uid 추출
//            long uid;
//            try {
//                uid = jwtService.getUserIdx();
//            } catch (BaseException exception) {
//                uid = 0;
//            }
//
//            int page = 0;
//            if (p != null) page = Integer.parseInt(p);
//
//            long parsed_cid = Long.parseLong(cid);
//
//            return new BaseResponse<>(recommenderProvider.getVideoSummaryList(parsed_cid, uid, page));
//        } catch (BaseException exception) {
//            return new BaseResponse<>((exception.getStatus()));
//        } catch (NumberFormatException exception) {
//            logger.error(exception.getMessage());
//            return new BaseResponse<>(REQUEST_ERROR);
//        }
//    }
}
