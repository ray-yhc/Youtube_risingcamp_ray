package com.example.demo.src.channel;


import com.example.demo.config.BaseException;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;

/**
 * Service란?
 * Controller에 의해 호출되어 실제 비즈니스 로직과 트랜잭션을 처리: Create, Update, Delete 의 로직 처리
 * 요청한 작업을 처리하는 관정을 하나의 작업으로 묶음
 * dao를 호출하여 DB CRUD를 처리 후 Controller로 반환
 */
@Service    // [Business Layer에서 Service를 명시하기 위해서 사용] 비즈니스 로직이나 respository layer 호출하는 함수에 사용된다.
// [Business Layer]는 컨트롤러와 데이터 베이스를 연결
public class ChannelService {
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log 처리부분: Log를 기록하기 위해 필요한 함수입니다.

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************
    private final ChannelDao channelDao;
    private final ChannelProvider channelProvider;
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    @Autowired //readme 참고
    public ChannelService(ChannelDao channelDao, ChannelProvider channelProvider, JwtService jwtService) {
        this.channelDao = channelDao;
        this.channelProvider = channelProvider;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!

    }
    // ******************************************************************************


    /**
     * 채널 구독
     */
    public void addSubscribeChannel(long cid, long uid) throws BaseException {
        try{
            channelDao.addSubscribeChannel(cid, uid);
        } catch (BaseException baseException) {
            throw baseException;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 채널 구독 취소
     */
    public void cancelSubscribeChannel(long cid, long uid) throws BaseException {
        try{
            channelDao.cancelSubscribeChannel(cid, uid);
        } catch (BaseException baseException) {
            throw baseException;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 채널 알림설정
     */
    public void addAlertChannel(long cid, long uid) throws BaseException {
        try{
            channelDao.addAlertChannel(cid, uid);
        } catch (BaseException baseException) {
            throw baseException;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 채널 알림 취소
     */
    public void cancelAlertChannel(long cid, long uid) throws BaseException {
        try{
            channelDao.cancelAlertChannel(cid, uid);
        } catch (BaseException baseException) {
            throw baseException;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }

}
