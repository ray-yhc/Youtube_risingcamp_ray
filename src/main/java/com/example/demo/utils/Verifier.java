package com.example.demo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class Verifier {
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log 처리부분: Log를 기록하기 위해 필요한 함수입니다.

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************
    private final VerifierDao verifierDao;

//    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    @Autowired //readme 참고
    public Verifier(VerifierDao verifierDao) {
        this.verifierDao = verifierDao;
    }
    // ******************************************************************************



    /**
     * 채널 cid 검증
     */
    public boolean verifyChannelId (long cid){
        try {
            verifierDao.verifyChannelId(cid);
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
            verifierDao.verifyVideoId(vid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * 영상 viewId 검증
     */
    public boolean verifyViewId (long viewId) {
        try {
            verifierDao.verifyViewId(viewId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * LanguageId 검증
     */
    public boolean verifyLanguageId (int languageId) {
        try {
            verifierDao.verifyLanguageId(languageId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 사용자가 접속 가능한 영상인지 확인
     */
    public boolean authorizeVideo (long vid, long uid) {
        try {
            verifierDao.authorizeVideo(vid,uid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
