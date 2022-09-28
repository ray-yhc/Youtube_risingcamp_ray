package com.example.demo.src.video;


import com.example.demo.config.BaseException;
import com.example.demo.src.user.model.*;
import com.example.demo.src.video.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

@Repository //  [Persistence Layer에서 DAO를 명시하기 위해 사용]

/**
 * DAO란?
 * 데이터베이스 관련 작업을 전담하는 클래스
 * 데이터베이스에 연결하여, 입력 , 수정, 삭제, 조회 등의 작업을 수행
 */
public class VideoDao {

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************

    private JdbcTemplate jdbcTemplate;

    @Autowired //readme 참고
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    // ******************************************************************************

    /**
     * DAO관련 함수코드의 전반부는 크게 String ~~~Query와 Object[] ~~~~Params, jdbcTemplate함수로 구성되어 있습니다.(보통은 동적 쿼리문이지만, 동적쿼리가 아닐 경우, Params부분은 없어도 됩니다.)
     * Query부분은 DB에 SQL요청을 할 쿼리문을 의미하는데, 대부분의 경우 동적 쿼리(실행할 때 값이 주입되어야 하는 쿼리) 형태입니다.
     * 그래서 Query의 동적 쿼리에 입력되어야 할 값들이 필요한데 그것이 Params부분입니다.
     * Params부분은 클라이언트의 요청에서 제공하는 정보(~~~~Req.java에 있는 정보)로 부터 getXXX를 통해 값을 가져옵니다. ex) getEmail -> email값을 가져옵니다.
     *      Notice! get과 get의 대상은 카멜케이스로 작성됩니다. ex) item -> getItem, password -> getPassword, email -> getEmail, userIdx -> getUserIdx
     * 그 다음 GET, POST, PATCH 메소드에 따라 jabcTemplate의 적절한 함수(queryForObject, query, update)를 실행시킵니다(DB요청이 일어납니다.).
     *      Notice!
     *      POST, PATCH의 경우 jdbcTemplate.update
     *      GET은 대상이 하나일 경우 jdbcTemplate.queryForObject, 대상이 복수일 경우, jdbcTemplate.query 함수를 사용합니다.
     * jdbcTeplate이 실행시킬 때 Query 부분과 Params 부분은 대응(값을 주입)시켜서 DB에 요청합니다.
     * <p>
     * 정리하자면 < 동적 쿼리문 설정(Query) -> 주입될 값 설정(Params) -> jdbcTemplate함수(Query, Params)를 통해 Query, Params를 대응시켜 DB에 요청 > 입니다.
     * <p>
     * <p>
     * DAO관련 함수코드의 후반부는 전반부 코드를 실행시킨 후 어떤 결과값을 반환(return)할 것인지를 결정합니다.
     * 어떠한 값을 반환할 것인지 정의한 후, return문에 전달하면 됩니다.
     * ex) return this.jdbcTemplate.query( ~~~~ ) -> ~~~~쿼리문을 통해 얻은 결과를 반환합니다.
     */

    /**
     * 참고 링크
     * https://jaehoney.tistory.com/34 -> JdbcTemplate 관련 함수에 대한 설명
     * https://velog.io/@seculoper235/RowMapper%EC%97%90-%EB%8C%80%ED%95%B4 -> RowMapper에 대한 설명
     */


    // Video 테이블에서 영상 정보 가져오기
    public VideoInfoModel getVideoInfoById(long vid) {
        try {
            String getVideoInfoByIdQuery =
                    "SELECT id, title, channelId, thumbnailImg3xUrl,\n" +
                            "date_format(uploadedDate, '%Y. %c. %e.') AS 'uploadedDate',\n" +
                            "maxResolution, videoUrl, shareUrl, description\n" +
                            "FROM Video WHERE id=?";
            long getVideoInfoByIdParams = vid;
            return this.jdbcTemplate.queryForObject(getVideoInfoByIdQuery,
                    (rs, rowNum) -> new VideoInfoModel(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getInt("channelId"),
                            rs.getString("thumbnailImg3xUrl"),
                            rs.getString("uploadedDate"),
                            rs.getInt("maxResolution"),
                            rs.getString("videoUrl"),
                            rs.getString("shareUrl"),
                            rs.getString("description")
                    ), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                    getVideoInfoByIdParams); // 해당 닉네임을 갖는 모든 User 정보를 얻기 위해 jdbcTemplate 함수(Query, 객체 매핑 정보, Params)의 결과 반환
        } catch (IncorrectResultSizeDataAccessException error) {
            return new VideoInfoModel(0,
                    "",
                    0,
                    "",
                    "",
                    0,
                    "",
                    "",
                    ""
            );
        }
    }

    // 영상 좋아요 수 조회
    public Integer getLikeCountById(long vid) {
        try {
            String getLikeCountByIdQuery =
                    "SELECT videoId, count(*) AS 'likeCount' FROM LikeVideo\n" +
                            "WHERE videoId=?\n" +
                            "GROUP BY videoId";
            long getLikeCountByIdParams = vid;
            return this.jdbcTemplate.queryForObject(getLikeCountByIdQuery,
                    (rs, rowNum) -> rs.getInt("likeCount"), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                    getLikeCountByIdParams);
        } catch (IncorrectResultSizeDataAccessException error) {
            return 0;
        }
    }

    // 영상 조회수 조회
    public Integer getViewCountById(long vid) {
        try {
            String getViewCountByIdQuery =
                    "SELECT videoId,count(*) AS 'viewCount' from ViewVideo\n" +
                            "WHERE videoId=?\n" +
                            "group by videoId";
            long getViewCountByIdParams = vid;
            return this.jdbcTemplate.queryForObject(getViewCountByIdQuery,
                    (rs, rowNum) -> rs.getInt("viewCount"), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                    getViewCountByIdParams);
        } catch (IncorrectResultSizeDataAccessException error) {
            return 0;
        }
    }

    // 채널 정보 조회
    public ChannelInfoModel getChannelInfoById(long cid) {
        try {
            String Query = "SELECT name, channelImgUrl, watermarkImgUrl, subscribe  FROM Channel\n" +
                    "-- 구독자 수 카운터 추가\n" +
                    "LEFT JOIN (SELECT subscribedId, count(*) AS 'subscribe' FROM Subscribe\n" +
                    "GROUP BY subscribedId) subscribeCounter\n" +
                    "ON subscribeCounter.subscribedId = Channel.id\n" +
                    "-- 채널 id 필터림\n" +
                    "WHERE id=?";
            long Params = cid;

            return this.jdbcTemplate.queryForObject(Query,
                    (rs, rowNum) -> new ChannelInfoModel(
                            rs.getString("name"),
                            rs.getString("channelImgUrl"),
                            rs.getString("watermarkImgUrl"),
                            rs.getInt("subscribe")
                    ),
                    Params);
        } catch (IncorrectResultSizeDataAccessException error) {
            return new ChannelInfoModel("",
                    "",
                    "",
                    0
            );
        }
    }

    // 영상 해시태그 리스트 조회
    public List<String> getHashtagById(long vid) {
        String Query = "SELECT videoId, H.name AS 'hashtag'\n" +
                "FROM Video\n" +
                "-- 해시-영상 맵 추가\n" +
                "LEFT JOIN HashtagVideoMap HVM on Video.id = HVM.videoId\n" +
                "-- 해시태그 추가\n" +
                "LEFT JOIN Hashtag H on HVM.hashtagId = H.id\n" +
                "WHERE Video.id=?";
        long Params = vid;

        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> rs.getString("hashtag"),
                Params);
    }

    // 영상 자막 리스트 조회
    public List<String> getSubtitlesById(long vid) {
        String Query = "SELECT Video.id, L.name AS 'subscriptLanguage'\n" +
                "FROM Video\n" +
                "-- 자막 정보 추가\n" +
                "JOIN ClosedCaption CC on Video.id = CC.videoId\n" +
                "-- 언어 정보 추가\n" +
                "LEFT JOIN Language L on CC.languageId = L.id\n" +
                "WHERE Video.id=?";
        long Params = vid;

        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> rs.getString("subscriptLanguage"),
                Params);
    }

    // 영상 댓글정보(간략히) 조회
    public CommentSummaryInfoModel getCommentSummaryInfoById(long vid) {
        try {
            String Query =
                    "SELECT groupComment.videoId AS 'videoId', C.channelImgUrl AS 'commentorImgUrl',\n" +
                            "Comment.description AS 'rescentCommentDescription', commentCount FROM Comment\n" +
                            "-- 댓글 수 카운트, 최신댓글 조회\n" +
                            "INNER JOIN (SELECT videoId, count(*) AS 'commentCount', max(Comment.updated) AS 'last' FROM Comment\n" +
                            "    GROUP BY videoId) groupComment\n" +
                            "ON groupComment.last = Comment.updated\n" +
                            "-- 댓글 작성자 채널 연결\n" +
                            "LEFT JOIN Channel C ON Comment.writorId = C.id\n" +
                            "WHERE Comment.videoId =?";
            long Params = vid;

            return this.jdbcTemplate.queryForObject(Query,
                    (rs, rowNum) -> new CommentSummaryInfoModel(
                            rs.getInt("commentCount"),
                            rs.getString("commentorImgUrl"),
                            rs.getString("rescentCommentDescription")
                    ),
                    Params);
        } catch (IncorrectResultSizeDataAccessException error) {
            return new CommentSummaryInfoModel(
                    0,
                    "",
                    ""
            );
        }
    }

    // 시청 시작 데이터 생성
    public long createViewStart(long vid, long uid, String start) {
        String Query = "INSERT INTO ViewVideo (videoId,viewerId,startPoint) VALUES (?,?,?)";
        this.jdbcTemplate.update(Query, vid, uid, start);

        return this.jdbcTemplate.queryForObject("SELECT last_insert_id()",
                long.class);
    }

    // 시청시작 포인트 조회
    public String getViewStartPoint(long viewid) {
        String Query = "SELECT * FROM ViewVideo WHERE id=?";

        return this.jdbcTemplate.queryForObject(Query,
                (rs, rowNum) -> rs.getString("startPoint"),
                viewid);
    }

    // 시청 끝 데이터 수정
    public void updateViewEnd(long vid, long userid, long viewid, int watchedTime, String endViewPoint) {
        String Query = "UPDATE ViewVideo SET endPoint=?, watchedTimeSec=? WHERE id =?";

        this.jdbcTemplate.update(Query,
                endViewPoint, watchedTime, viewid);
    }

    /**
     * 새로운 영상 insert
     */
    public long uploadNewVideo(PostSettingReq p, long uid, String videoUrl,
                               String thumbnailImg1xUrl, String thumbnailImg2xUrl, String thumbnailImg3xUrl,
                               String shareUrl) {
        String Query = "INSERT INTO Video (title, channelId, \n" +
                "                   thumbnailImg1xUrl, thumbnailImg2xUrl, thumbnailImg3xUrl,\n" +
                "                   videoLength, uploadedDate,maxResolution, \n" +
                "                   shareUrl, videoUrl, description,\n" +
                "                   languageId, category)\n" +
                "            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Object[] Params = new Object[]{
                p.getTitle(),
                uid,
                thumbnailImg1xUrl, thumbnailImg2xUrl, thumbnailImg3xUrl,
                p.getVideoLength(),
                p.getUploadedDate(),
                p.getMaxResolution(),
                shareUrl, videoUrl,
                p.getDescription(),
                p.getLanguageId(),
                p.getCategory()
        };

        this.jdbcTemplate.update(Query, Params);

        String lastInserIdQuery = "select last_insert_id()"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInserIdQuery, long.class);
    }

    /**
     * 해시태그 입력
     */
    public void addHashTags(long vid, List<String> hashtags) {
        long hashid;
        for (String h : hashtags) {
            try {
                // 이미 저장된 해시태그인지 확인
                hashid = this.jdbcTemplate.queryForObject(
                        "SELECT * FROM Hashtag WHERE name = ?",
                        (rs, rowNum) -> rs.getLong("id"),
                        h
                );
            } catch (IncorrectResultSizeDataAccessException error) {
                //새로운 해시태그 생성
                this.jdbcTemplate.update(
                        "INSERT INTO Hashtag (name) VALUES (?)",
                        h
                );
                // 생성된 해시태그의 id 추출
                hashid = this.jdbcTemplate.queryForObject(
                        "SELECT last_insert_id()",
                        long.class);
            }

            // 영상의 id와 해시태그의 id 입력
            this.jdbcTemplate.update(
                    "INSERT INTO HashtagVideoMap (videoId, hashtagId) VALUE (?,?)",
                    vid, hashid);
        }
    }

    /**
     * 영상 세부정보 조회
     */
    public GetSettingRes getVideoSettingInfo(long vid) throws BaseException {
        try {
            String Query = "SELECT * FROM Video WHERE id=?";
            return this.jdbcTemplate.queryForObject(Query,
                    (rs, rowNum) -> new GetSettingRes(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getLong("channelId"),
                            rs.getString("thumbnailImg1xUrl"),
                            rs.getString("videoLength"),
                            rs.getString("uploadedDate"),
                            rs.getInt("maxResolution"),
                            rs.getString("shareUrl"),
                            rs.getString("videoUrl"),
                            rs.getString("description"),
                            rs.getLong("languageId"),
                            rs.getString("shorts"),
                            rs.getString("primiered"),
                            rs.getString("streaming"),
                            rs.getString("streamStatus"),
                            rs.getString("kidContent"),
                            rs.getString("ageLimited"),
                            rs.getString("productPlacement"),
                            rs.getString("recordedDate"),
                            rs.getString("recordedLocation"),
                            rs.getString("licence"),
                            rs.getString("allowShare"),
                            rs.getString("alertToSubscribers"),
                            rs.getString("allowSamplingClip"),
                            rs.getString("category"),
                            rs.getString("allowComment"),
                            rs.getString("sortComment"),
                            rs.getString("showLike"),
                            rs.getString("accessStatus")
                    ),
                    vid);
        } catch (IncorrectResultSizeDataAccessException error) {
            throw new BaseException(REQUEST_ERROR);
        }
    }

    /**
     * 영상 원본파일 위치 가져오기
     */
    public String getVideoUrl(long vid) {
        try {
            String Query = "SELECT id,videoUrl FROM Video WHERE id=?";
            return this.jdbcTemplate.queryForObject(
                    Query,
                    (rs, rowNum) -> rs.getString("videoUrl"),
                    vid
            );
        } catch (IncorrectResultSizeDataAccessException error) {
            return "";
        }
    }

    /**
     * 영상 데이터 inactive
     */
    public int inactiveVideo(long vid) {
        String Query = "UPDATE Video SET status = 'inactive' WHERE id =?";
        return this.jdbcTemplate.update(Query, vid);
    }


    /**
     * 영상정보 수정 _ title
     */
    public int updateSettingTitle(long vid, String title) {
        String Query = "UPDATE Video SET title = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, title, vid);
    }

    /**
     * 영상정보 수정 _ thumbnail
     */
    public int updateSettingThumbnail(long vid,
                                      String thumbnailImg1xUrl,
                                      String thumbnailImg2xUrl,
                                      String thumbnailImg3xUrl) {
        String Query = "UPDATE Video SET thumbnailImg1xUrl = ?,thumbnailImg2xUrl = ?,thumbnailImg3xUrl = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query,
                thumbnailImg1xUrl, thumbnailImg2xUrl, thumbnailImg3xUrl,
                vid);
    }

    /**
     * 영상정보 수정 _ uploadedDate
     */
    public int updateSettingUploadedDate(long vid, String uploadedDate) {
        String Query = "UPDATE Video SET uploadedDate = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, uploadedDate, vid);
    }

    /**
     * 영상정보 수정 _ description
     */
    public int updateSettingDescription(long vid, String description) {
        String Query = "UPDATE Video SET description = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, description, vid);
    }

    /**
     * 영상정보 수정 _ languageId
     */
    public int updateSettingLanguage(long vid, long languageId) {
        String Query = "UPDATE Video SET languageId = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, languageId, vid);
    }

    /**
     * 영상정보 수정 _ category
     */
    public int updateSettingCategory(long vid, String category) {
        String Query = "UPDATE Video SET category = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, category, vid);
    }

    /**
     * 영상정보 수정 _ shorts
     */
    public int updateSettingShorts(long vid, String shorts) {
        String Query = "UPDATE Video SET shorts = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, shorts, vid);
    }

    /**
     * 영상정보 수정 _ primiered
     */
    public int updateSettingPrimiered(long vid, String primiered) {
        String Query = "UPDATE Video SET primiered = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, primiered, vid);
    }

    /**
     * 영상정보 수정 _ streaming
     */
    public int updateSettingStreamingInfo(long vid, String streaming, String streamStatus) {
        String Query = "UPDATE Video SET streaming = ?, streamStatus = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, streaming, streamStatus, vid);
    }

    /**
     * 영상정보 수정 _ kidContentkidContent
     */
    public int updateSettingKidContent(long vid, String kidContent) {
        String Query = "UPDATE Video SET kidContent = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, kidContent, vid);
    }

    /**
     * 영상정보 수정 _ ageLimited
     */
    public int updateSettingAgeLimited(long vid, String ageLimited) {
        String Query = "UPDATE Video SET ageLimited = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, ageLimited, vid);
    }

    /**
     * 영상정보 수정 _ productPlacement
     */
    public int updateSettingProductPlacement(long vid, String ppl) {
        String Query = "UPDATE Video SET productPlacement = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, ppl, vid);
    }

    /**
     * 영상정보 수정 _ recordedDate
     */
    public int updateSettingRecordedDate(long vid, String recordedDate) {
        String Query = "UPDATE Video SET recordedDate = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, recordedDate, vid);
    }

    /**
     * 영상정보 수정 _ recordedLocation
     */
    public int updateSettingRecordedLocation(long vid, String location) {
        String Query = "UPDATE Video SET recordedLocation = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, location, vid);
    }

    /**
     * 영상정보 수정 _ licence
     */
    public int updateSettingLicence(long vid, String licence) {
        String Query = "UPDATE Video SET licence = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, licence, vid);
    }

    /**
     * 영상정보 수정 _ allowShare
     */
    public int updateSettingAllowShare(long vid, String allowShare) {
        String Query = "UPDATE Video SET allowShare = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, allowShare, vid);
    }

    /**
     * 영상정보 수정 _ alertToSubscribers
     */
    public int updateSettingAlertToSubscribers(long vid, String alertToSubscribers) {
        String Query = "UPDATE Video SET alertToSubscribers = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, alertToSubscribers, vid);
    }

    /**
     * 영상정보 수정 _ allowSamplingClip
     */
    public int updateSettingAllowSamplingClip(long vid, String allowSamplingClip) {
        String Query = "UPDATE Video SET allowSamplingClip = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, allowSamplingClip, vid);
    }

    /**
     * 영상정보 수정 _ allowComment
     */
    public int updateSettingAllowComment(long vid, String allowComment) {
        String Query = "UPDATE Video SET allowComment = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, allowComment, vid);
    }

    /**
     * 영상정보 수정 _ sortComment
     */
    public int updateSettingSortComment(long vid, String sortComment) {
        String Query = "UPDATE Video SET sortComment = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, sortComment, vid);
    }

    /**
     * 영상정보 수정 _ showLike
     */
    public int updateSettingShowLike(long vid, String showLike) {
        String Query = "UPDATE Video SET showLike = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, showLike, vid);
    }

    /**
     * 영상정보 수정 _ accessStatus
     */
    public int updateSettingAccessStatus(long vid, String accessStatus) {
        String Query = "UPDATE Video SET accessStatus = ? WHERE id = ? ";
        return this.jdbcTemplate.update(Query, accessStatus, vid);
    }

    /**
     * 좋아요 추가
     */
    public void addLikeVideo(long vid, long cid) throws BaseException {
        try {
            String searchQuery = "SELECT *, COUNT(*) AS 'count' FROM LikeVideo\n" +
                    "WHERE status='active' AND videoId=? AND viewerId=?\n" +
                    "GROUP BY viewerId";
            int count = this.jdbcTemplate.queryForObject(searchQuery,
                    (rs, rowNum) -> rs.getInt("count"),
                    vid, cid);

            if (count > 0) {
                // 에러가 나지 않으면 데이터가 이미 있는 것이므로 에러 반환
                throw new BaseException(EXISTING_DATA);
            }
        } catch (IncorrectResultSizeDataAccessException error) {
            // 에러 발생하면 다음쿼리 진행
            String addQuery = "INSERT INTO LikeVideo (videoId, viewerId) VALUES (?,?)";
            this.jdbcTemplate.update(addQuery, vid, cid);
        }
    }

    /**
     * 좋아요 취소
     */
    public void cancelLikeVideo(long vid, long cid) throws BaseException {
        try {
            String searchQuery = "SELECT *, COUNT(*) AS 'count' FROM LikeVideo\n" +
                    "WHERE status='active' AND videoId=? AND viewerId=?\n" +
                    "GROUP BY viewerId";
            int count = this.jdbcTemplate.queryForObject(searchQuery,
                    (rs, rowNum) -> rs.getInt("count"),
                    vid, cid);

            if (count > 0) {
                // 데이터가 있으므로 inactive
                String delQuery = "UPDATE LikeVideo SET status='inactive'\n" +
                        "WHERE status='active' AND videoId=? AND viewerId=?";
                if( this.jdbcTemplate.update(delQuery,vid,cid) == 0)
                    throw new BaseException(REQUEST_ERROR);
            }
            else{
                throw new BaseException(REQUEST_ERROR);
            }
        } catch (IncorrectResultSizeDataAccessException error) {
            // 데이터가 없으면 에러 반환
            throw new BaseException(NON_EXISTING_DATA);
        }
    }

    /**
     * 싫어요 추가
     */
    public void addDislikeVideo(long vid, long cid) throws BaseException {
        try {
            String searchQuery = "SELECT *, COUNT(*) AS 'count' FROM DislikeVideo\n" +
                    "WHERE status='active' AND videoId=? AND viewerId=?\n" +
                    "GROUP BY viewerId";
            int count = this.jdbcTemplate.queryForObject(searchQuery,
                    (rs, rowNum) -> rs.getInt("count"),
                    vid, cid);

            if (count > 0) {
                // 에러가 나지 않으면 데이터가 이미 있는 것이므로 에러 반환
                throw new BaseException(EXISTING_DATA);
            }
        } catch (IncorrectResultSizeDataAccessException error) {
            // 에러 발생하면 다음쿼리 진행
            String addQuery = "INSERT INTO DislikeVideo (videoId, viewerId) VALUES (?,?)";
            this.jdbcTemplate.update(addQuery, vid, cid);
        }
    }

    /**
     * 싫어요 취소
     */
    public void cancelDislikeVideo(long vid, long cid) throws BaseException {
        try {
            String searchQuery = "SELECT *, COUNT(*) AS 'count' FROM DislikeVideo\n" +
                    "WHERE status='active' AND videoId=? AND viewerId=?\n" +
                    "GROUP BY viewerId";
            int count = this.jdbcTemplate.queryForObject(searchQuery,
                    (rs, rowNum) -> rs.getInt("count"),
                    vid, cid);

            if (count > 0) {
                // 데이터가 있으므로 inactive
                String delQuery = "UPDATE DislikeVideo SET status='inactive'\n" +
                        "WHERE status='active' AND videoId=? AND viewerId=?";
                if( this.jdbcTemplate.update(delQuery,vid,cid) == 0)
                    throw new BaseException(REQUEST_ERROR);
            }
            else{
                throw new BaseException(REQUEST_ERROR);
            }
        } catch (IncorrectResultSizeDataAccessException error) {
            // 데이터가 없으면 에러 반환
            throw new BaseException(NON_EXISTING_DATA);
        }

    }


    /**
     * 채널 cid 검증
     */
    public void verifyChannelId (long cid){
        String From = "Channel";
        String Query = "SELECT * FROM "+ From +" WHERE status='active' AND id=?";
        this.jdbcTemplate.queryForObject(Query,
                (rs, rowNum) -> rs.getLong("id"),
                cid);
    }
    /**
     * 영상 vid 검증
     */
    public void verifyVideoId (long vid) {
        String From = "Video";
        String Query = "SELECT * FROM "+ From +" WHERE status='active' AND id=?";
        this.jdbcTemplate.queryForObject(Query,
                (rs, rowNum) -> rs.getLong("id"),
                vid);
    }
    /**
     * 영상 viewId 검증
     */
    public void verifyViewId (long viewId) {
        String From = "ViewVideo";
        String Query = "SELECT * FROM "+ From +" WHERE status='active' AND id=?";
        this.jdbcTemplate.queryForObject(Query,
                (rs, rowNum) -> rs.getLong("id"),
                viewId);
    }
    /**
     * LanguageId 검증
     */
    public void verifyLanguageId (int languageId) {
        String From = "Language";
        String Query = "SELECT * FROM "+ From +" WHERE status='active' AND id=?";
        this.jdbcTemplate.queryForObject(Query,
                (rs, rowNum) -> rs.getLong("id"),
                languageId);
    }
    /**
     * 사용자가 접속 가능한 영상인지 확인
     */
    public void authorizeVideo (long vid, long uid) {
        String Query = "SELECT * FROM Video WHERE status='active' AND id=? AND channelId=?";
        this.jdbcTemplate.queryForObject(Query,
                (rs, rowNum) -> rs.getLong("id"),
                vid,uid);
    }
}
