package com.example.demo.src.comment;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.comment.model.GetCommentRes;
import com.example.demo.src.comment.model.PostCommentReq;
import com.example.demo.src.comment.model.PostCommmentRes;
import com.example.demo.src.video.model.GetVideoRes;
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
@RequestMapping("/app/comment")
// method가 어떤 HTTP 요청을 처리할 것인가를 작성한다.
// 요청에 대해 어떤 Controller, 어떤 메소드가 처리할지를 맵핑하기 위한 어노테이션
// URL(/app/users)을 컨트롤러의 메서드와 매핑할 때 사용
/**
 * Controller란?
 * 사용자의 Request를 전달받아 요청의 처리를 담당하는 Service, Prodiver 를 호출
 */
public class CommentController {
    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************

    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log를 남기기: 일단은 모르고 넘어가셔도 무방합니다.

    @Autowired  // 객체 생성을 스프링에서 자동으로 생성해주는 역할. 주입하려 하는 객체의 타입이 일치하는 객체를 자동으로 주입한다.
    // IoC(Inversion of Control, 제어의 역전) / DI(Dependency Injection, 의존관계 주입)에 대한 공부하시면, 더 깊이 있게 Spring에 대한 공부를 하실 수 있을 겁니다!(일단은 모르고 넘어가셔도 무방합니다.)
    // IoC 간단설명,  메소드나 객체의 호출작업을 개발자가 결정하는 것이 아니라, 외부에서 결정되는 것을 의미
    // DI 간단설명, 객체를 직접 생성하는 게 아니라 외부에서 생성한 후 주입 시켜주는 방식
    private final CommentProvider commentProvider;
    @Autowired
    private final CommentService commentService;
    @Autowired
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    public CommentController(CommentProvider commentProvider, CommentService commentService, JwtService jwtService) {
        this.commentProvider = commentProvider;
        this.commentService = commentService;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }

    // ******************************************************************************

    /**
     * 댓글 조회하기
     * [GET] /app/comment?vid=
     */
    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetCommentRes>> getCommentList(@RequestParam(required = false) Long vid,
                                                            @RequestParam(required = false) Long commentid,
                                                            @RequestParam(required = false) Integer p) {
        try {
            if (commentid == null && vid == null) // vid와 commentid가 모두 없다면 error 반환
                return new BaseResponse<>(EMPTY_COMMENT_QUERY_PARAMS);

            if (p == null) p = 0;

            if (vid != null) { // 영상 댓글 조회
                // vid 검증
                if (!commentProvider.verifyVideoId(vid))
                    throw new BaseException(INVALID_VIDEO_ID);

                List<GetCommentRes> getCommentRes = commentProvider.getCommentsByVideo(vid, p);
                return new BaseResponse<>(getCommentRes);
            } else if (commentid != null) { // 댓글의 대댓글 조회
                // commentid 검증
                if (!commentProvider.verifyCommentId(commentid))
                    throw new BaseException(INVALID_COMMENT_ID);

                List<GetCommentRes> getCommentRes = commentProvider.getCommentsByComment(commentid, p);
                return new BaseResponse<>(getCommentRes);
            }
            return new BaseResponse<>(REQUEST_ERROR);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 댓글 작성하기
     * [POST] /app/comment?vid=
     */
    @ResponseBody
    @PostMapping("")
    public BaseResponse<PostCommmentRes> createComment(@RequestBody PostCommentReq postCommentReq,
                                                       @RequestParam() Long vid,
                                                       @RequestParam(required = false) Long commentid) {
        try {
            // jwt 에서 cid 추출
            long uid = jwtService.getUserIdx();
            // uid 검증
            if (!commentProvider.verifyChannelId(uid))
                throw new BaseException(INVALID_CHANNEL);
            // vid 검증
            if (!commentProvider.verifyVideoId(vid))
                throw new BaseException(INVALID_VIDEO_ID);

            if (commentid != null) { // 대댓글
                // commentid 검증
                if (!commentProvider.verifyCommentId(commentid))
                    throw new BaseException(INVALID_COMMENT_ID);
                PostCommmentRes postCommmentRes = commentService.createReplyComment(vid, uid, commentid, postCommentReq);
                return new BaseResponse<>(postCommmentRes);
            } else {
                //댓글
                PostCommmentRes postCommmentRes = commentService.createComment(vid, uid, postCommentReq);
                return new BaseResponse<>(postCommmentRes);
            }
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

}
