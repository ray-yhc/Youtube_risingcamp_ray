package com.example.demo.src.channel;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.channel.model.ChannelHeadInfoModel;
import com.example.demo.src.channel.model.GetChannelRes;
import com.example.demo.src.channel.model.GetVideoSummaryRes;
import com.example.demo.src.channel.model.UserSubscribeInfoModel;
import com.example.demo.src.comment.model.ChannelSummaryInfoModel;
import com.example.demo.src.comment.model.CommentInfoModel;
import com.example.demo.src.comment.model.GetCommentRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

//Provider : Read의 비즈니스 로직 처리
@Service    // [Business Layer에서 Service를 명시하기 위해서 사용] 비즈니스 로직이나 respository layer 호출하는 함수에 사용된다.
// [Business Layer]는 컨트롤러와 데이터 베이스를 연결
/**
 * Provider란?
 * Controller에 의해 호출되어 실제 비즈니스 로직과 트랜잭션을 처리: Read의 비즈니스 로직 처리
 * 요청한 작업을 처리하는 관정을 하나의 작업으로 묶음
 * dao를 호출하여 DB CRUD를 처리 후 Controller로 반환
 */
public class ChannelProvider {


    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************
    private final ChannelDao channelDao;
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired //readme 참고
    public ChannelProvider(ChannelDao channelDao, JwtService jwtService) {
        this.channelDao = channelDao;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }
    // ******************************************************************************


    // 채널 배너 정보 조회
    public GetChannelRes getChannelInfo(long cid, long uid) throws BaseException {
        try {
            //채널정보 탐색
            ChannelHeadInfoModel channelHeadInfoModel = channelDao.getChannelHeadInfo(cid);
            // 불러온 채널id가 0 -> 정보를 불러오지 못함 -> 2000 오류 발생!
            if(channelHeadInfoModel.getChannelId() == 0) throw new BaseException(INVALID_CHANNEL_ID);

            GetChannelRes getChannelRes = new GetChannelRes(
                    channelHeadInfoModel.getChannelId(),
                    channelHeadInfoModel.getChannelName(),
                    channelHeadInfoModel.getChannelImgUrl(),
                    channelHeadInfoModel.getBannerImgUrl(),
                    channelHeadInfoModel.getChannelSubscribeCount(),
                    false, false
            );

            if (uid != 0) { // uid가 있으면 구독/알림정보 탐색
                UserSubscribeInfoModel userSubscribeInfoModel = channelDao.getUserSubscribeInfo(cid, uid);

                if (userSubscribeInfoModel.isSubscribing()) getChannelRes.setSubscribing(true);
                if (userSubscribeInfoModel.isAlert()) getChannelRes.setAlert(true);
            }

            return getChannelRes;
        } catch (BaseException exception) {
            throw new BaseException(exception.getStatus());
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 채널 동영상 리스트 조회
    public List<GetVideoSummaryRes> getVideoSummaryList(long cid, long uid, int page) throws BaseException {
        try{
            int start = page * 30;
            int length = 30;

            List<GetVideoSummaryRes> getVideoSummaryResList = channelDao.getVideoSummaryList(cid,start,length);

            for( GetVideoSummaryRes g : getVideoSummaryResList ){
                long vid = g.getVideoId();
                g.setViewCount(channelDao.getViewCountById(vid));

                if (uid != 0) { // uid가 있으면 조회정보 탐색
                    g.setUserViewPoint(channelDao.getUserViewPoint(vid,uid));
                }
            }
            return getVideoSummaryResList;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }


    /**
     * 채널 cid 검증
     */
    public boolean verifyChannelId (long cid){
        try {
            channelDao.verifyChannelId(cid);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }
    /**
     * 영상 vid 검증
     */
    public boolean verifyVideoId (long vid) {
        try {
            channelDao.verifyVideoId(vid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
