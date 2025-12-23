package com.tech.novagraphbackendmodel.graph.constant;

import com.tech.novagraphbackendcommon.utils.CacheUtils;

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
     * 图片点赞数量记录 screenplayId:count
     */
    public static final String THUMB_KEY_SCREENPLAY_PREFIX = "thumb:screenplay:";

    /**
     * 剧本缓存
     */
    public static final String Screenplay_CACHE_PREFIX = "ngsp:";

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
     * 图片点赞数量记录 pictureId:count
     */
    public static String getPictureThumbKey(Long pictureId){
        return THUMB_KEY_SCREENPLAY_PREFIX + pictureId.toString();
    }

    /**
     * 拼接 redis 的 Key 用于分布式 Redis 区分不同服务
     */
    public static String buildRedisKey(String key){
        return CacheUtils.APP_NAME + ":" + key;
    }

    public static String getScreenplayCacheKey(String key){
        return Screenplay_CACHE_PREFIX + key;
    }
}
