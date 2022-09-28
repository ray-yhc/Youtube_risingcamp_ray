package com.example.demo.src.recommender;

import com.example.demo.config.BaseException;
import com.example.demo.src.channel.model.ChannelHeadInfoModel;
import com.example.demo.src.channel.model.GetChannelRes;
import com.example.demo.src.channel.model.GetVideoSummaryRes;
import com.example.demo.src.channel.model.UserSubscribeInfoModel;
import com.example.demo.src.recommender.model.ChannelInfoModel;
import com.example.demo.src.recommender.model.GetVideoInfoRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;
import static com.example.demo.config.BaseResponseStatus.REQUEST_ERROR;

//Provider : Read의 비즈니스 로직 처리
@Service    // [Business Layer에서 Service를 명시하기 위해서 사용] 비즈니스 로직이나 respository layer 호출하는 함수에 사용된다.
// [Business Layer]는 컨트롤러와 데이터 베이스를 연결
/**
 * Provider란?
 * Controller에 의해 호출되어 실제 비즈니스 로직과 트랜잭션을 처리: Read의 비즈니스 로직 처리
 * 요청한 작업을 처리하는 관정을 하나의 작업으로 묶음
 * dao를 호출하여 DB CRUD를 처리 후 Controller로 반환
 */
public class RecommenderProvider {


    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************
    private final RecommenderDao recommenderDao;
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired //readme 참고
    public RecommenderProvider(RecommenderDao recommenderDao, JwtService jwtService) {
        this.recommenderDao = recommenderDao;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!
    }
    // ******************************************************************************

    /**
     * 추천 영상 리스트 조회 (20개씩) - uid 있을 때
     */
    public List<GetVideoInfoRes> mainRecommender (long uid, int p) throws BaseException {
        try {
            int start = p * 20;
            int length = 20;

            // 관련 채널 리스트 불러오기
            List<Integer> channelList = recommenderDao.getChannelList(uid);
            String channelListCommand = "";
            for(int i=0; i<channelList.size(); i++){
                if (i==0)
                    channelListCommand += channelList.get(i);
                else
                    channelListCommand += "," + channelList.get(i);
            }

            System.out.println(channelListCommand);

            // 추천 영상 리스트 불러오기
            List<GetVideoInfoRes> getVideoInfoResList = recommenderDao.getVideoRecommenderByUser(channelListCommand,start,length);

            for (GetVideoInfoRes g : getVideoInfoResList) {
                long vid = g.getVideoId();
                long cid = g.getChannelId();

                // 조회수 불러오기
                g.setViewCount(recommenderDao.getViewCountById(vid));

                // 채널정보 추가
                ChannelInfoModel cInfo =  recommenderDao.getChannelInfo(cid);
                g.setChannelName(cInfo.getChannelName());
                g.setChannelImgUrl(cInfo.getChannelImgUrl());

                // 유저 시청정보 탐색
                g.setUserViewPoint(recommenderDao.getUserViewPoint(vid, uid));
            }
            return getVideoInfoResList;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }



    /**
     * 추천 영상 리스트 조회 (20개씩) - uid 없을 때
     */
    public List<GetVideoInfoRes> mainRecommender (int p) throws BaseException {
        try{
            int start = p * 20;
            int length = 20;

            // 추천 영상 리스트 불러오기
            List<GetVideoInfoRes> getVideoInfoResList = recommenderDao.getVideoRecommender(start,length);

            for (GetVideoInfoRes g : getVideoInfoResList) {
                long vid = g.getVideoId();
                long cid = g.getChannelId();

                // 조회수 불러오기
                g.setViewCount(recommenderDao.getViewCountById(vid));

                // 채널정보 추가
                ChannelInfoModel cInfo =  recommenderDao.getChannelInfo(cid);
                g.setChannelName(cInfo.getChannelName());
                g.setChannelImgUrl(cInfo.getChannelImgUrl());
            }
            return getVideoInfoResList;
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
            recommenderDao.verifyChannelId(cid);
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
            recommenderDao.verifyVideoId(vid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
//    // 채널 동영상 리스트 조회
//    public List<GetVideoSummaryRes> getVideoSummaryList(long cid, long uid, int page) throws BaseException {
//        try {
//            int start = page * 30;
//            int length = 30;
//
//            List<GetVideoSummaryRes> getVideoSummaryResList = channelDao.getVideoSummaryList(cid, start, length);
//
//            for (GetVideoSummaryRes g : getVideoSummaryResList) {
//                long vid = g.getVideoId();
//                g.setViewCount(channelDao.getViewCountById(vid));
//
//                if (uid != 0) { // uid가 있으면 조회정보 탐색
//                    g.setUserViewPoint(channelDao.getUserViewPoint(vid, uid));
//                }
//            }
//            return getVideoSummaryResList;
//        } catch (Exception exception) {
//            logger.error(exception.getMessage());
//            throw new BaseException(DATABASE_ERROR);
//        }
//    }

}
