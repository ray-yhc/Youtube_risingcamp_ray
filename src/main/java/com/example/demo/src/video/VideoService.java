package com.example.demo.src.video;


import com.example.demo.config.BaseException;
import com.example.demo.src.video.model.*;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.demo.config.BaseResponseStatus.*;

/**
 * Service란?
 * Controller에 의해 호출되어 실제 비즈니스 로직과 트랜잭션을 처리: Create, Update, Delete 의 로직 처리
 * 요청한 작업을 처리하는 관정을 하나의 작업으로 묶음
 * dao를 호출하여 DB CRUD를 처리 후 Controller로 반환
 */
@Service    // [Business Layer에서 Service를 명시하기 위해서 사용] 비즈니스 로직이나 respository layer 호출하는 함수에 사용된다.
// [Business Layer]는 컨트롤러와 데이터 베이스를 연결
public class VideoService {
    final Logger logger = LoggerFactory.getLogger(this.getClass()); // Log 처리부분: Log를 기록하기 위해 필요한 함수입니다.

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************
    private final VideoDao videoDao;
    private final VideoProvider videoProvider;
    private final JwtService jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!


    @Autowired //readme 참고
    public VideoService(VideoDao videoDao, VideoProvider videoProvider, JwtService jwtService) {
        this.videoDao = videoDao;
        this.videoProvider = videoProvider;
        this.jwtService = jwtService; // JWT부분은 7주차에 다루므로 모르셔도 됩니다!

    }
    // ******************************************************************************

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
     * 영상시청 시작
     * -> 조회 시작 포인트 기록
     */
    public long viewStart(long vid, long uid, PostViewStartReq postViewStartReq) throws BaseException {
        try {
            return videoDao.createViewStart(vid,
                    uid,
                    postViewStartReq.getStartViewPoint());

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 영상시청 끝
     * -> 조회 시작 포인트 불러오기
     * -> 시청시간 계산
     * -> 조회 끝 포인트, 시청시간 기록
     */
    public void viewEnd(long vid, long uid, PatchViewEndReq patchViewEndReq) throws BaseException {
        try {
            // startPoint 조회하기
            String startViewPoint = videoDao.getViewStartPoint(patchViewEndReq.getViewId());
            String endViewPoint = patchViewEndReq.getEndViewPoint();

            // watchedTime 계산하기
            int watchedTime =
                    3600 * (Integer.parseInt(endViewPoint.substring(0, 2))
                            - Integer.parseInt(startViewPoint.substring(0, 2)))
                            + 60 * (Integer.parseInt(endViewPoint.substring(3, 5))
                            - Integer.parseInt(startViewPoint.substring(3, 5)))
                            + (Integer.parseInt(endViewPoint.substring(6, 8))
                            - Integer.parseInt(startViewPoint.substring(6, 8)));

            // endTime 수정하기
            videoDao.updateViewEnd(vid, uid,
                    patchViewEndReq.getViewId(),
                    watchedTime,
                    patchViewEndReq.getEndViewPoint());

        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }


    @Transactional
    /**
     * 새 영상 업로드
     * -> 영상 필수 값들만 등록
     * -> 영상 id 반환
     * -> Nullable 값들 중 입력받은 값들 update
     * -> 제목/내용 분석 -> 해시태그 찾아내기
     * -> 해시태그 등록
     */
    public PostSettingRes uploadNewVideo(PostSettingReq p, long uid) throws BaseException {
        // title, thumbnailFileName, videoFileName, description 길이 validate
        if (p.getTitle().length() > 100)
            throw new BaseException(TOO_LONG_TITLE);
        if (p.getThumbnailFileName().length() > 100)
            throw new BaseException(TOO_LONG_THUMBNAILFILENAME);
        if (p.getVideoFileName().length() > 100)
            throw new BaseException(TOO_LONG_VIDEOFILENAME);
        if (p.getDescription().length() > 1000)
            throw new BaseException(TOO_LONG_DESCRIPTION);
        // 시간,날짜 포맷 validate
        if (!checkTime(p.getVideoLength()))
            throw new BaseException(INVALID_VIDEOLENGTH_FORMAT);
        if (!checkDate(p.getUploadedDate()))
            throw new BaseException(INVALID_UPLOADED_FORMAT);

        // 카테고리 validate
        switch (p.getCategory()) {
            case "게임": case "과학기술": case "교육": case "노하우/스타일": case "뉴스/정치": case "비영리/사회운동": case "스포츠":
            case "애완동물/동물": case "엔터테인먼트": case "영화/애니메이션": case "음악": case "인물/블로그": case "자동차/교통": case "코미디":
                break;
            default:
                throw new BaseException(INVALID_CATEGORY);
        }
        // languageId validate
        if (! videoProvider.verifyLanguageId(p.getLanguageId()))
            throw new BaseException(INVALID_LANGUAGE_ID);


        // 영상 저장소에 저장하는 작업
        // ...
        String videoUrl = "https://youtube/…/video/" + p.getVideoFileName();
        // 썸네일 x1 x2 x3 크기로 저장하는 작업
        // ...
        String thumbnailImg1xUrl = "https://youtube/…/thumbnail/thumbnail_1x_" + p.getThumbnailFileName();
        String thumbnailImg2xUrl = "https://youtube/…/thumbnail/thumbnail_2x_" + p.getThumbnailFileName();
        String thumbnailImg3xUrl = "https://youtube/…/thumbnail/thumbnail_3x_" + p.getThumbnailFileName();
        // 공유주소 생성하는 작업
        // ...
        String shareUrl = "https://youtu.be/xxxxxxxxxxxxxxxx";


        try {
            // 영상 필수값들 등록
            long vid = videoDao.uploadNewVideo(p, uid, videoUrl, thumbnailImg1xUrl, thumbnailImg2xUrl, thumbnailImg3xUrl, shareUrl);

            // Nullable 값들 중 입력받은 값들 update
            if (!p.getShorts().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getShorts()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_SHORTS);
                }
                // 데이터 수정
                videoDao.updateSettingShorts(vid, p.getShorts());
            }
            if (!p.getPrimiered().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getPrimiered()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_PRIMIERED);
                }
                // 데이터 수정
                videoDao.updateSettingPrimiered(vid, p.getPrimiered());
            }
            if (!p.getStreaming().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getStreaming()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_STREAMING);
                }
                // 올바른 입력값인지 확인
                switch (p.getStreamStatus()) {
                    case "false":
                    case "before":
                    case "onair":
                    case "finished":
                        break;
                    default:
                        throw new BaseException(INVALID_STREAMSTATUS);
                }
                // 데이터 수정
                videoDao.updateSettingStreamingInfo(vid, p.getStreaming(), p.getStreamStatus());
            }
            if (!p.getKidContent().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getKidContent()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_KIDCONTENTS);
                }
                // 데이터 수정
                videoDao.updateSettingKidContent(vid, p.getKidContent());
            }
            if (!p.getAgeLimited().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getAgeLimited()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_AGELIMITED);
                }
                // 데이터 수정
                videoDao.updateSettingAgeLimited(vid, p.getAgeLimited());
            }
            if (!p.getProductPlacement().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getProductPlacement()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_PPL);
                }
                // 데이터 수정
                videoDao.updateSettingProductPlacement(vid, p.getProductPlacement());
            }
            if (!p.getRecordedDate().equals("")) {
                // 올바른 입력값인지 확인
                if (!checkDate(p.getRecordedDate()))
                    throw new BaseException(INVALID_RECORDED_FORMAT);
                // 데이터 수정
                videoDao.updateSettingRecordedDate(vid, p.getRecordedDate());
            }
            if (!p.getRecordedLocation().equals("")) {
                // 올바른 입력값인지 확인
                if (p.getRecordedLocation().length() > 100)
                    throw new BaseException(TOO_LONG_LOCATION);
                // 데이터 수정
                videoDao.updateSettingRecordedLocation(vid, p.getRecordedLocation());
            }
            if (!p.getLicence().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getLicence()) {
                    case "Youtube Standard Liscence":
                    case "Creative Commons":
                        break;
                    default:
                        throw new BaseException(INVALID_LICENCE);
                }
                // 데이터 수정
                videoDao.updateSettingLicence(vid, p.getLicence());
            }
            if (!p.getAllowShare().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getAllowShare()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_ALLOWSHARE);
                }
                // 데이터 수정
                videoDao.updateSettingAllowShare(vid, p.getAllowShare());
            }
            if (!p.getAlertToSubscribers().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getAlertToSubscribers()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_ALERT);
                }
                // 데이터 수정
                videoDao.updateSettingAlertToSubscribers(vid, p.getAlertToSubscribers());
            }
            if (!p.getAllowSamplingClip().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getAllowSamplingClip()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_ALLOWCLIP);
                }
                // 데이터 수정
                videoDao.updateSettingAllowSamplingClip(vid, p.getAllowSamplingClip());
            }
            if (!p.getAllowComment().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getAllowComment()) {
                    case "allow":
                    case "pause_some":
                    case "pause_all":
                    case "disallow":
                        break;
                    default:
                        throw new BaseException(INVALID_ALLOWCOMMENT);
                }
                // 데이터 수정
                videoDao.updateSettingAllowComment(vid, p.getAllowComment());
            }
            if (!p.getSortComment().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getSortComment()) {
                    case "popularity":
                    case "recent":
                        break;
                    default:
                        throw new BaseException(INVALID_SORTCOMMENT);
                }
                // 데이터 수정
                videoDao.updateSettingSortComment(vid, p.getSortComment());
            }
            if (!p.getShowLike().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getShowLike()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_SHOWLIKE);
                }
                // 데이터 수정
                videoDao.updateSettingShowLike(vid, p.getShowLike());
            }
            if (!p.getAccessStatus().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getShowLike()) {
                    case "listed":
                    case "unlisted":
                    case "ristricted":
                        break;
                    default:
                        throw new BaseException(INVALID_ACCESS);
                }
                // 데이터 수정
                videoDao.updateSettingAccessStatus(vid, p.getAccessStatus());
            }

            // 해시태그 추출
            List<String> hashTags = extractHashTag(p.getTitle(), p.getDescription());
            videoDao.addHashTags(vid, hashTags);

            // 공유주소, 영상 아이디 반환
            return new PostSettingRes(vid, shareUrl);

        } catch (BaseException exception) {
            // todo: 에러 발생하면 다시 데이터 지워주기
            throw exception;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 문자열에서 해시태그 추출하는 메소드
     */
    private List<String> extractHashTag(String title, String des) {

        Pattern p = Pattern.compile("\\#([0-9a-zA-Z가-힣]*)");
        List<String> resultList = new ArrayList<>();

        // 제목 추적
        Matcher m = p.matcher(title);
        String extractHashTag = null;
        while (m.find()) {
            extractHashTag = sepcialCharacter_replace(m.group());

            if (extractHashTag != null)
                resultList.add(extractHashTag);
        }

        // 본문 추적
        m = p.matcher(des);
        while (m.find()) {
            extractHashTag = sepcialCharacter_replace(m.group());

            if (extractHashTag != null)
                resultList.add(extractHashTag);
        }

        return resultList;
    }

    /**
     * 문자열에서 특수문자 제거하는 메소드
     */
    private String sepcialCharacter_replace(String str) {
        String result = str.replaceAll("[-_+=!@#[$]%\\^&[*]\\(\\)\\[\\]\\{\\}[|];:'\"<>,.?/~`）//r]", "");
        if (result.length() < 1)
            return null;
        return result;
    }

    /**
     * 영상 세부정보 수정
     */
    public int updateVideoSettingInfo(PatchSettingReq p, long vid) throws BaseException {
        try {

            if (!p.getTitle().equals("")) {
                // 올바른 입력값인지 확인
                if (p.getTitle().length() > 100)
                    throw new BaseException(TOO_LONG_TITLE);
                // 데이터 수정
                videoDao.updateSettingTitle(vid, p.getTitle());
            }
            if (!p.getThumbnailFileName().equals("")) {
                // 올바른 입력값인지 확인
                if (p.getThumbnailFileName().length() > 100)
                    throw new BaseException(TOO_LONG_THUMBNAILFILENAME);
                // 썸네일 x1 x2 x3 크기로 저장하는 작업
                // ...
                String thumbnailImg1xUrl = "https://youtube/…/thumbnail/thumbnail_1x_" + p.getThumbnailFileName();
                String thumbnailImg2xUrl = "https://youtube/…/thumbnail/thumbnail_2x_" + p.getThumbnailFileName();
                String thumbnailImg3xUrl = "https://youtube/…/thumbnail/thumbnail_3x_" + p.getThumbnailFileName();
                videoDao.updateSettingThumbnail(vid, thumbnailImg1xUrl, thumbnailImg2xUrl, thumbnailImg3xUrl);
            }
            if (!p.getUploadedDate().equals("")) {
                // 올바른 입력값인지 확인
                if (!checkDate(p.getUploadedDate()))
                    throw new BaseException(INVALID_UPLOADED_FORMAT);
                // 데이터 수정
                videoDao.updateSettingUploadedDate(vid, p.getUploadedDate());
            }
            if (!p.getDescription().equals("")) {
                // 올바른 입력값인지 확인
                if (p.getDescription().length() > 1000)
                    throw new BaseException(TOO_LONG_DESCRIPTION);
                // 데이터 수정
                videoDao.updateSettingDescription(vid, p.getDescription());
            }
            if (p.getLanguageId() != 0) {
                // languageId validate
                if (! videoProvider.verifyLanguageId(p.getLanguageId()))
                    throw new BaseException(INVALID_LANGUAGE_ID);
                // 데이터 수정
                videoDao.updateSettingLanguage(vid, p.getLanguageId());
            }
            if (!p.getCategory().equals("")) {
                // 카테고리 validate
                switch (p.getCategory()) {
                    case "게임": case "과학기술": case "교육": case "노하우/스타일": case "뉴스/정치": case "비영리/사회운동": case "스포츠":
                    case "애완동물/동물": case "엔터테인먼트": case "영화/애니메이션": case "음악": case "인물/블로그": case "자동차/교통": case "코미디":
                        break;
                    default:
                        throw new BaseException(INVALID_CATEGORY);
                }
                videoDao.updateSettingCategory(vid, p.getCategory());
            }
            if (!p.getShorts().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getShorts()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_SHORTS);
                }
                // 데이터 수정
                videoDao.updateSettingShorts(vid, p.getShorts());
            }
            if (!p.getPrimiered().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getPrimiered()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_PRIMIERED);
                }
                // 데이터 수정
                videoDao.updateSettingPrimiered(vid, p.getPrimiered());
            }
            if (!p.getStreaming().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getStreaming()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_STREAMING);
                }
                // 올바른 입력값인지 확인
                switch (p.getStreamStatus()) {
                    case "false":
                    case "before":
                    case "onair":
                    case "finished":
                        break;
                    default:
                        throw new BaseException(INVALID_STREAMSTATUS);
                }
                // 데이터 수정
                videoDao.updateSettingStreamingInfo(vid, p.getStreaming(), p.getStreamStatus());
            }
            if (!p.getKidContent().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getKidContent()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_KIDCONTENTS);
                }
                // 데이터 수정
                videoDao.updateSettingKidContent(vid, p.getKidContent());
            }
            if (!p.getAgeLimited().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getAgeLimited()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_AGELIMITED);
                }
                // 데이터 수정
                videoDao.updateSettingAgeLimited(vid, p.getAgeLimited());
            }
            if (!p.getProductPlacement().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getProductPlacement()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_PPL);
                }
                // 데이터 수정
                videoDao.updateSettingProductPlacement(vid, p.getProductPlacement());
            }
            if (!p.getRecordedDate().equals("")) {
                // 올바른 입력값인지 확인
                if (!checkDate(p.getRecordedDate()))
                    throw new BaseException(INVALID_RECORDED_FORMAT);
                // 데이터 수정
                videoDao.updateSettingRecordedDate(vid, p.getRecordedDate());
            }
            if (!p.getRecordedLocation().equals("")) {
                // 올바른 입력값인지 확인
                if (p.getRecordedLocation().length() > 100)
                    throw new BaseException(TOO_LONG_LOCATION);
                // 데이터 수정
                videoDao.updateSettingRecordedLocation(vid, p.getRecordedLocation());
            }
            if (!p.getLicence().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getLicence()) {
                    case "Youtube Standard Liscence":
                    case "Creative Commons":
                        break;
                    default:
                        throw new BaseException(INVALID_LICENCE);
                }
                // 데이터 수정
                videoDao.updateSettingLicence(vid, p.getLicence());
            }
            if (!p.getAllowShare().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getAllowShare()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_ALLOWSHARE);
                }
                // 데이터 수정
                videoDao.updateSettingAllowShare(vid, p.getAllowShare());
            }
            if (!p.getAlertToSubscribers().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getAlertToSubscribers()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_ALERT);
                }
                // 데이터 수정
                videoDao.updateSettingAlertToSubscribers(vid, p.getAlertToSubscribers());
            }
            if (!p.getAllowSamplingClip().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getAllowSamplingClip()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_ALLOWCLIP);
                }
                // 데이터 수정
                videoDao.updateSettingAllowSamplingClip(vid, p.getAllowSamplingClip());
            }
            if (!p.getAllowComment().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getAllowComment()) {
                    case "allow":
                    case "pause_some":
                    case "pause_all":
                    case "disallow":
                        break;
                    default:
                        throw new BaseException(INVALID_ALLOWCOMMENT);
                }
                // 데이터 수정
                videoDao.updateSettingAllowComment(vid, p.getAllowComment());
            }
            if (!p.getSortComment().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getSortComment()) {
                    case "popularity":
                    case "recent":
                        break;
                    default:
                        throw new BaseException(INVALID_SORTCOMMENT);
                }
                // 데이터 수정
                videoDao.updateSettingSortComment(vid, p.getSortComment());
            }
            if (!p.getShowLike().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getShowLike()) {
                    case "true":
                    case "false":
                        break;
                    default:
                        throw new BaseException(INVALID_SHOWLIKE);
                }
                // 데이터 수정
                videoDao.updateSettingShowLike(vid, p.getShowLike());
            }
            if (!p.getAccessStatus().equals("")) {
                // 올바른 입력값인지 확인
                switch (p.getShowLike()) {
                    case "listed":
                    case "unlisted":
                    case "ristricted":
                        break;
                    default:
                        throw new BaseException(INVALID_ACCESS);
                }
                // 데이터 수정
                videoDao.updateSettingAccessStatus(vid, p.getAccessStatus());
            }
//            if (!p.getShorts().equals("")) videoDao.updateSettingShorts(vid, p.getShorts());
//            if (!p.getPrimiered().equals("")) videoDao.updateSettingPrimiered(vid, p.getPrimiered());
//            if (!p.getStreaming().equals(""))
//                videoDao.updateSettingStreamingInfo(vid, p.getStreaming(), p.getStreamStatus());
//            if (!p.getKidContent().equals("")) videoDao.updateSettingKidContent(vid, p.getKidContent());
//            if (!p.getAgeLimited().equals("")) videoDao.updateSettingAgeLimited(vid, p.getAgeLimited());
//            if (!p.getProductPlacement().equals(""))
//                videoDao.updateSettingProductPlacement(vid, p.getProductPlacement());
//            if (!p.getRecordedDate().equals("")) videoDao.updateSettingRecordedDate(vid, p.getRecordedDate());
//            if (!p.getRecordedLocation().equals(""))
//                videoDao.updateSettingRecordedLocation(vid, p.getRecordedLocation());
//            if (!p.getLicence().equals("")) videoDao.updateSettingLicence(vid, p.getLicence());
//            if (!p.getAllowShare().equals("")) videoDao.updateSettingAllowShare(vid, p.getAllowShare());
//            if (!p.getAlertToSubscribers().equals(""))
//                videoDao.updateSettingAlertToSubscribers(vid, p.getAlertToSubscribers());
//            if (!p.getAllowSamplingClip().equals(""))
//                videoDao.updateSettingAllowSamplingClip(vid, p.getAllowSamplingClip());
//            if (!p.getAllowComment().equals("")) videoDao.updateSettingAllowComment(vid, p.getAllowComment());
//            if (!p.getSortComment().equals("")) videoDao.updateSettingSortComment(vid, p.getSortComment());
//            if (!p.getShowLike().equals("")) videoDao.updateSettingShowLike(vid, p.getShowLike());
//            if (!p.getAccessStatus().equals("")) videoDao.updateSettingAccessStatus(vid, p.getAccessStatus());

            return 1;
        } catch (BaseException exception) {
            // todo: 에러 발생하면 다시 데이터 지워주기
            throw exception;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 영상 삭제
     * -> 영상 원본파일 삭제
     * -> 영상 데이터 inactive
     */
    public int deleteVideo(long vid) throws BaseException {
        try {
            // 영상 원본파일 위치 가져오기
            String videoUrl = videoDao.getVideoUrl(vid);
            // 영상 원본 삭제
            // ...

            // 영상 데이터 inactive
            return videoDao.inactiveVideo(vid);
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 좋아요 추가
     */
    public void addLikeVideo(long vid, long cid) throws BaseException {
        try {
            videoDao.addLikeVideo(vid, cid);
        } catch (BaseException baseException) {
            throw baseException;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);

        }
    }

    public void cancelLikeVideo(long vid, long cid) throws BaseException {
        try {
            videoDao.cancelLikeVideo(vid, cid);
        } catch (BaseException baseException) {
            throw baseException;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void addDislikeVideo(long vid, long cid) throws BaseException {
        try {
            videoDao.addDislikeVideo(vid, cid);
        } catch (BaseException baseException) {
            throw baseException;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void cancelDislikeVideo(long vid, long cid) throws BaseException {
        try {
            videoDao.cancelDislikeVideo(vid, cid);
        } catch (BaseException baseException) {
            throw baseException;
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            logger.error(exception.getMessage());
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
