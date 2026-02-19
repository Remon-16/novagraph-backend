package com.tech.novagraphbackendmodel.graph.constant;

import cn.hutool.json.JSONUtil;
import com.tech.novagraphbackendcommon.utils.CacheUtils;
import org.springframework.util.DigestUtils;

public class ScreenplayCacheConstant {
    /**
     * 用户给剧本点赞 hash key
     */
    public static final String USER_SCREENPLAY_THUMB_KEY_PREFIX = "sp:thumb:";

    public static final Long UN_THUMB_CONSTANT = 0L;

    /**
     * 临时 点赞记录 key
     */
    public static final String TEMP_THUMB_KEY_PREFIX = "sp:thumb:temp:";

    /**
     * 剧本点赞数量记录 screenplayId:count
     */
    public static final String THUMB_KEY_SCREENPLAY_PREFIX = "thumb:screenplay:";

    /**
     * 剧本缓存
     */
    public static final String SCREENPLAY_CACHE_PREFIX = "ngsp:";

    /**
     * 剧本查询缓存
     */
    public static final String SCREENPLAY_QUERY_CACHE_PREFIX = "ngsp:query:";

    /**
     * 剧本评论缓存
     */
    public static final String SCREENPLAY_COMMENT_CACHE_PREFIX = "ngsp:cmt:";

    /**
     * 剧本评论 zSet缓存名称：ngsp:comment:sorted:(desc/asc):SCREENPLAYId
     */
    public static final String SCREENPLAY_COMMENT_SORTED_CACHE_PREFIX = "ngsp:cmt:sorted";
    /**
     * 剧本评论 评论总数缓存名称 ngsp:comment:sorted:total:SCREENPLAYId
     */
    public static final String SCREENPLAY_COMMENT_SORTED_TOTAL_CACHE_PREFIX = "ngsp:cmt:sorted:total";
    /**
     * 剧本二级评论 zSet缓存名称 comment:second:sorted:(desc/asc):commentId
     */
    public static final String SCREENPLAY_SECOND_COMMENT_SORTED_CACHE_PREFIX = "ngsp:cmt:sc:sorted";
    /**
     * 剧本二级评论 总数缓存名称 comment:second:sorted:total:commentId
     */
    public static final String SCREENPLAY_SECOND_COMMENT_SORTED_TOTAL_CACHE_PREFIX = "ngsp:cmt:sc:sorted:total";


    public static String getUserThumbKey(Long userId) {
        return USER_SCREENPLAY_THUMB_KEY_PREFIX + userId.toString();
    }

    /**
     * 获取 临时点赞记录 key
     */
    public static String getTempThumbKey(String time) {
        return TEMP_THUMB_KEY_PREFIX + time;
    }

    /**
     * 剧本点赞数量记录 screenplayId:count
     */
    public static String getScreenplayThumbKey(Long screenplayId){
        return THUMB_KEY_SCREENPLAY_PREFIX + screenplayId.toString();
    }

    public static String getScreenplaySortedCommentCacheKey(String order, Long screenplayId){
        return SCREENPLAY_COMMENT_SORTED_CACHE_PREFIX + order + ":" + screenplayId;
    }

    public static String getScreenplaySecondCommentSortedCache(String order, Long commentId){
        return SCREENPLAY_SECOND_COMMENT_SORTED_CACHE_PREFIX + order + ":" + commentId;
    }

    public static String getScreenplayCommentCacheKey(String commentId){
        return SCREENPLAY_COMMENT_CACHE_PREFIX + commentId;
    }

    public static String getScreenplayCommentSortedTotalCache(Long screenplayId){
        return SCREENPLAY_COMMENT_SORTED_TOTAL_CACHE_PREFIX + ":" + screenplayId;
    }

    public static String getScreenplaySecondCommentSortedTotalCache(Long commentId){
        return SCREENPLAY_SECOND_COMMENT_SORTED_TOTAL_CACHE_PREFIX + ":" + commentId;
    }

    /**
     * 拼接 redis 的 Key 用于分布式 Redis 区分不同服务
     */
    public static String buildRedisKey(String key){
        return CacheUtils.APP_NAME + ":" + key;
    }

    public static String getScreenplayCacheKey(String key){
        return SCREENPLAY_CACHE_PREFIX + key;
    }

    public static String getScreenplayQueryCacheKey(Object queryCondition){
        String queryConditionString = JSONUtil.toJsonStr(queryCondition);
        String hashKey = DigestUtils.md5DigestAsHex(queryConditionString.getBytes());
        return SCREENPLAY_QUERY_CACHE_PREFIX + hashKey;
    }


}
