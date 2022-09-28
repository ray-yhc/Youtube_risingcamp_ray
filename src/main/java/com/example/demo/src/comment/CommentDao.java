package com.example.demo.src.comment;


import com.example.demo.src.comment.model.ChannelSummaryInfoModel;
import com.example.demo.src.comment.model.CommentInfoModel;
import com.example.demo.src.comment.model.PostCommmentRes;
import com.example.demo.src.video.model.ChannelInfoModel;
import com.example.demo.src.video.model.CommentSummaryInfoModel;
import com.example.demo.src.video.model.VideoInfoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Repository //  [Persistence Layer에서 DAO를 명시하기 위해 사용]

/**
 * DAO란?
 * 데이터베이스 관련 작업을 전담하는 클래스
 * 데이터베이스에 연결하여, 입력 , 수정, 삭제, 조회 등의 작업을 수행
 */
public class CommentDao {

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

    public List<CommentInfoModel> getCommentsInfoByVideo(long vid, int start, int length) {
        try {
            String Query = "SELECT id,videoId,writorId,description,commentId, depth,\n" +
                    "-- 업로드날짜 표시 형식\n" +
                    "CASE\n" +
                    "    WHEN TIMESTAMPDIFF (MINUTE,Comment.created, CURRENT_TIMESTAMP) < 60\n" +
                    "    THEN CONCAT(TIMESTAMPDIFF (MINUTE,Comment.created, CURRENT_TIMESTAMP), '분 전')\n" +
                    "    WHEN TIMESTAMPDIFF(HOUR,Comment.created, CURRENT_TIMESTAMP) < 24\n" +
                    "    THEN CONCAT(TIMESTAMPDIFF(HOUR,Comment.created, CURRENT_TIMESTAMP), '시간 전')\n" +
                    "    WHEN TIMESTAMPDIFF(DAY,Comment.created, CURRENT_TIMESTAMP)< 30\n" +
                    "    THEN CONCAT(TIMESTAMPDIFF(DAY,Comment.created, CURRENT_TIMESTAMP), '일 전')\n" +
                    "    WHEN TIMESTAMPDIFF(MONTH,Comment.created, CURRENT_TIMESTAMP)< 12\n" +
                    "    THEN CONCAT(TIMESTAMPDIFF(MONTH,Comment.created, CURRENT_TIMESTAMP), '개월 전')\n" +
                    "    ELSE CONCAT(TIMESTAMPDIFF(YEAR,Comment.created, CURRENT_TIMESTAMP ), '년 전')\n" +
                    "END AS 'uploaded'\n" +
                    "from Comment\n" +
                    "WHERE videoId = ? AND depth IS NULL\n" +
                    "ORDER BY Comment.created DESC\n" +
                    "LIMIT ?,?";

            return this.jdbcTemplate.query(Query,
                    (rs, rowNum) -> new CommentInfoModel(
                            rs.getLong("id"),
                            rs.getLong("videoId"),
                            rs.getLong("writorId"),
                            rs.getString("description"),
                            rs.getString("uploaded")
                    ),
                    vid, start, length);
        } catch (IncorrectResultSizeDataAccessException error) {
            return Collections.emptyList();
        }
    }

    public List<CommentInfoModel> getCommentsInfoByComment(long commentid, int start, int length) {
        try {
            String Query = "SELECT id,videoId,writorId,description,commentId, depth,\n" +
                    "-- 업로드날짜 표시 형식\n" +
                    "CASE\n" +
                    "    WHEN TIMESTAMPDIFF (MINUTE,Comment.created, CURRENT_TIMESTAMP) < 60\n" +
                    "    THEN CONCAT(TIMESTAMPDIFF (MINUTE,Comment.created, CURRENT_TIMESTAMP), '분 전')\n" +
                    "    WHEN TIMESTAMPDIFF(HOUR,Comment.created, CURRENT_TIMESTAMP) < 24\n" +
                    "    THEN CONCAT(TIMESTAMPDIFF(HOUR,Comment.created, CURRENT_TIMESTAMP), '시간 전')\n" +
                    "    WHEN TIMESTAMPDIFF(DAY,Comment.created, CURRENT_TIMESTAMP)< 30\n" +
                    "    THEN CONCAT(TIMESTAMPDIFF(DAY,Comment.created, CURRENT_TIMESTAMP), '일 전')\n" +
                    "    WHEN TIMESTAMPDIFF(MONTH,Comment.created, CURRENT_TIMESTAMP)< 12\n" +
                    "    THEN CONCAT(TIMESTAMPDIFF(MONTH,Comment.created, CURRENT_TIMESTAMP), '개월 전')\n" +
                    "    ELSE CONCAT(TIMESTAMPDIFF(YEAR,Comment.created, CURRENT_TIMESTAMP ), '년 전')\n" +
                    "END AS 'uploaded'\n" +
                    "from Comment\n" +
                    "WHERE commentId = ?\n" +
                    "ORDER BY Comment.created DESC\n" +
                    "LIMIT ?,?";

            return this.jdbcTemplate.query(Query,
                    (rs, rowNum) -> new CommentInfoModel(
                            rs.getLong("id"),
                            rs.getLong("videoId"),
                            rs.getLong("writorId"),
                            rs.getString("description"),
                            rs.getString("uploaded")
                    ),
                    commentid, start, length);
        } catch (IncorrectResultSizeDataAccessException error) {
            return Collections.emptyList();
        }
    }

    public ChannelSummaryInfoModel getChannelInfoByCid(long cid) {
        try {
            String Query = "SELECT channelImgUrl, name FROM Channel where id = ?";
            long Params = cid;
            return this.jdbcTemplate.queryForObject(Query,
                    (rs, rowNum) -> new ChannelSummaryInfoModel(
                            rs.getString("name"),
                            rs.getString("channelImgUrl")
                    ), Params);
        } catch (IncorrectResultSizeDataAccessException error) {
            return new ChannelSummaryInfoModel("", "");
        }
    }

    public int getReplyCountByComment(long commentid) {
        try {
            String Query = "SELECT commentId, count(*) AS 'replyCount' FROM Comment\n" +
                    "WHERE commentId = ?\n" +
                    "GROUP BY commentId";
            long Params = commentid;
            return this.jdbcTemplate.queryForObject(Query,
                    (rs, rowNum) -> rs.getInt("replyCount"),
                    Params);
        } catch (IncorrectResultSizeDataAccessException error) {
            return 0;
        }
    }


    public int getLikeCommentCountByComment(long commentid) {
        try {
            String Query = "SELECT commentId, count(*) AS 'likeCommentCount' FROM LikeComment\n" +
                    "WHERE commentId = ?\n" +
                    "GROUP BY commentId";
            long Params = commentid;
            return this.jdbcTemplate.queryForObject(Query,
                    (rs, rowNum) -> rs.getInt("likeCommentCount"),
                    Params);
        } catch (IncorrectResultSizeDataAccessException error) {
            return 0;
        }
    }

    public int getCommentDepth(long commentid) {
        try {
            String Query = "SELECT id, depth FROM Comment WHERE id = ?";
            return this.jdbcTemplate.queryForObject(Query,
                    (rs, rowNum) -> rs.getInt("depth"),
                    commentid);
        } catch (IncorrectResultSizeDataAccessException error) {
            return 0;
        }
    }

    public PostCommmentRes createComment(long vid, long userId, String description) {
        String Query = "INSERT INTO Comment (videoId,writorId,description) VALUES (?,?,?)";
        this.jdbcTemplate.update(Query,
                vid, userId, description);

        String lastInserIdQuery = "select last_insert_id() AS 'id'"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,
                (rs, rowNum) -> new PostCommmentRes(rs.getLong("id")));
    }

    public PostCommmentRes createReplyComment(long vid, long userId, String description, long commentId, int depth) {
        String Query = "INSERT INTO Comment (videoId,writorId,description,commentId,depth) VALUES (?,?,?,?,?)";
        this.jdbcTemplate.update(Query,
                vid, userId, description, commentId, depth);

        String lastInserIdQuery = "select last_insert_id() AS 'id'"; // 가장 마지막에 삽입된(생성된) id값은 가져온다.
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,
                (rs, rowNum) -> new PostCommmentRes(rs.getLong("id")));
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
    /**
     * 영상 commentid 검증
     */
    public void verifyCommentId (long commentid) {
        String Query = "SELECT * FROM Comment WHERE status='active' AND id=?";
        this.jdbcTemplate.queryForObject(Query,
                (rs, rowNum) -> rs.getLong("id"),
                commentid);
    }
//    try{
//      String Query;
//      String Params;
//      return this.jdbcTemplate._;
//    } catch (IncorrectResultSizeDataAccessException error) {
//    }
}



