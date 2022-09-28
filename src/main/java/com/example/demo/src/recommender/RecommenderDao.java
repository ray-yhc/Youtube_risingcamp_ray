package com.example.demo.src.recommender;


import com.example.demo.config.BaseException;
import com.example.demo.src.channel.model.ChannelHeadInfoModel;
import com.example.demo.src.channel.model.GetVideoSummaryRes;
import com.example.demo.src.channel.model.UserSubscribeInfoModel;
import com.example.demo.src.recommender.model.ChannelInfoModel;
import com.example.demo.src.recommender.model.GetVideoInfoRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

@Repository //  [Persistence Layer에서 DAO를 명시하기 위해 사용]

/**
 * DAO란?
 * 데이터베이스 관련 작업을 전담하는 클래스
 * 데이터베이스에 연결하여, 입력 , 수정, 삭제, 조회 등의 작업을 수행
 */
public class RecommenderDao {

    // *********************** 동작에 있어 필요한 요소들을 불러옵니다. *************************

    private JdbcTemplate jdbcTemplate;
    final Logger logger = LoggerFactory.getLogger(this.getClass());

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

    /**
     * 관련 채널 id 리스트 조회
     */
    public List<Integer> getChannelList(long uid) {
        String Query = "SELECT ViewVideo.id, videoId, channelId, viewerId, MAX(ViewVideo.created) AS 'created'\n" +
                "                     FROM ViewVideo\n" +
                "                              LEFT JOIN Video V on ViewVideo.videoId = V.id\n" +
                "                              LEFT JOIN Channel C on V.channelId = C.id\n" +
                "WHERE viewerId = ?\n" +
                "GROUP BY channelId\n" +
                "ORDER BY created DESC\n" +
                "LIMIT 10";
        return this.jdbcTemplate.query(Query,
                (rs, rowNum) -> rs.getInt("channelId"),
                uid);
    }

    /**
     * 관련 영상 리스트 조회 - uid 있을 때
     */
    public List<GetVideoInfoRes> getVideoRecommenderByUser(String channelListCommand,
                                                           int start, int length) {
        try {
            String Query = "SELECT id,thumbnailImg1xUrl, title, videoLength, shareUrl, channelId,\n" +
                    "-- 업로드날짜 표시 형식\n" +
                    "        CASE\n" +
                    "            WHEN TIMESTAMPDIFF (MINUTE,uploadedDate, CURRENT_TIMESTAMP) < 60\n" +
                    "            THEN CONCAT(TIMESTAMPDIFF (MINUTE,uploadedDate, CURRENT_TIMESTAMP), '분 전')\n" +
                    "            WHEN TIMESTAMPDIFF(HOUR,uploadedDate, CURRENT_TIMESTAMP) < 24\n" +
                    "            THEN CONCAT(TIMESTAMPDIFF(HOUR,uploadedDate, CURRENT_TIMESTAMP), '시간 전')\n" +
                    "            WHEN TIMESTAMPDIFF(DAY,uploadedDate, CURRENT_TIMESTAMP)< 30\n" +
                    "            THEN CONCAT(TIMESTAMPDIFF(DAY,uploadedDate, CURRENT_TIMESTAMP), '일 전')\n" +
                    "            WHEN TIMESTAMPDIFF(MONTH,uploadedDate, CURRENT_TIMESTAMP)< 12\n" +
                    "            THEN CONCAT(TIMESTAMPDIFF(MONTH,uploadedDate, CURRENT_TIMESTAMP), '개월 전')\n" +
                    "            ELSE CONCAT(TIMESTAMPDIFF(YEAR,uploadedDate, CURRENT_TIMESTAMP ), '년 전')\n" +
                    "        END AS 'uploaded'\n" +
                    "FROM Video\n" +
                    "WHERE status='active' AND channelId IN (" + channelListCommand + ") " +
                    "ORDER BY uploadedDate DESC\n" +
                    "LIMIT ?,?";
            return this.jdbcTemplate.query(Query,
                    (rs, rowNum) -> new GetVideoInfoRes(
                            rs.getInt("id"),
                            rs.getString("thumbnailImg1xUrl"),
                            rs.getString("title"),
                            rs.getString("videoLength"),
                            rs.getString("shareUrl"),
                            rs.getLong("channelId"),
                            "", "",
                            rs.getString("uploaded"),
                            0, ""
                    ),
                    start, length);
        } catch (IncorrectResultSizeDataAccessException error) {
            return Collections.emptyList();
        }
    }

    /**
     * 관련 영상 리스트 조회 - uid 없을 때
     */
    public List<GetVideoInfoRes> getVideoRecommender(int start, int length) {
        try {
            String Query = "SELECT id,thumbnailImg1xUrl, title, videoLength, shareUrl, channelId,\n" +
                    "-- 업로드날짜 표시 형식\n" +
                    "        CASE\n" +
                    "            WHEN TIMESTAMPDIFF (MINUTE,uploadedDate, CURRENT_TIMESTAMP) < 60\n" +
                    "            THEN CONCAT(TIMESTAMPDIFF (MINUTE,uploadedDate, CURRENT_TIMESTAMP), '분 전')\n" +
                    "            WHEN TIMESTAMPDIFF(HOUR,uploadedDate, CURRENT_TIMESTAMP) < 24\n" +
                    "            THEN CONCAT(TIMESTAMPDIFF(HOUR,uploadedDate, CURRENT_TIMESTAMP), '시간 전')\n" +
                    "            WHEN TIMESTAMPDIFF(DAY,uploadedDate, CURRENT_TIMESTAMP)< 30\n" +
                    "            THEN CONCAT(TIMESTAMPDIFF(DAY,uploadedDate, CURRENT_TIMESTAMP), '일 전')\n" +
                    "            WHEN TIMESTAMPDIFF(MONTH,uploadedDate, CURRENT_TIMESTAMP)< 12\n" +
                    "            THEN CONCAT(TIMESTAMPDIFF(MONTH,uploadedDate, CURRENT_TIMESTAMP), '개월 전')\n" +
                    "            ELSE CONCAT(TIMESTAMPDIFF(YEAR,uploadedDate, CURRENT_TIMESTAMP ), '년 전')\n" +
                    "        END AS 'uploaded'\n" +
                    "FROM Video\n" +
                    "WHERE status='active'\n" +
                    "ORDER BY uploadedDate DESC\n" +
                    "LIMIT ?,?";
            return this.jdbcTemplate.query(Query,
                    (rs, rowNum) -> new GetVideoInfoRes(
                            rs.getInt("id"),
                            rs.getString("thumbnailImg1xUrl"),
                            rs.getString("title"),
                            rs.getString("videoLength"),
                            rs.getString("shareUrl"),
                            rs.getLong("channelId"),
                            "", "",
                            rs.getString("uploaded"),
                            0, ""
                    ),
                    start, length);
        } catch (IncorrectResultSizeDataAccessException error) {
            return Collections.emptyList();
        }
    }

    /**
     * 채널정보 입력
     */
    public ChannelInfoModel getChannelInfo(long cid) {
        String Query = "SELECT name, channelImgUrl from Channel WHERE id = ?";
        return this.jdbcTemplate.queryForObject(Query,
                (rs, rowNum) -> new ChannelInfoModel(
                        rs.getString("name"),
                        rs.getString("channelImgUrl")
                ),
                cid);
    }


    /**
     * 영상 조회수 조회
     */
    public Integer getViewCountById(long vid) {
        try {
            String getViewCountByIdQuery =
                    "SELECT videoId,count(*) AS 'viewCount' from ViewVideo\n" +
                            "WHERE videoId=? AND status='active'\n" +
                            "group by videoId";
            long getViewCountByIdParams = vid;
            return this.jdbcTemplate.queryForObject(getViewCountByIdQuery,
                    (rs, rowNum) -> rs.getInt("viewCount"), // RowMapper(위의 링크 참조): 원하는 결과값 형태로 받기
                    getViewCountByIdParams);
        } catch (IncorrectResultSizeDataAccessException error) {
            return 0;
        }
    }

    /**
     * 유저의 시청 지점 조회
     */
    public String getUserViewPoint(long vid, long uid) {
        try {
            String Query = "SELECT videoId,viewerId,endPoint\n" +
                    "FROM ViewVideo\n" +
                    "WHERE status='active' and videoId=?  AND  viewerId=?\n" +
                    "ORDER BY created DESC\n" +
                    "LIMIT 1";

            return this.jdbcTemplate.queryForObject(Query,
                    (rs, rowNum) -> rs.getString("endPoint")
                    , vid, uid);
        } catch (IncorrectResultSizeDataAccessException error) {
            return "";
        }
    }

    /**
     * 채널 cid 검증
     */
    public void verifyChannelId (long cid){
        String Query = "SELECT * FROM Channel WHERE status='active' AND id=?";
        this.jdbcTemplate.queryForObject(Query,
                (rs, rowNum) -> rs.getLong("id"),
                cid);
    }
    /**
     * 영상 vid 검증
     */
    public void verifyVideoId (long vid) {
        String Query = "SELECT * FROM Video WHERE status='active' AND id=?";
        this.jdbcTemplate.queryForObject(Query,
                (rs, rowNum) -> rs.getLong("id"),
                vid);
    }

//    try{
//      String Query;
//      String Params;
//      return this.jdbcTemplate._;
//    } catch (IncorrectResultSizeDataAccessException error) {
//    }
}
