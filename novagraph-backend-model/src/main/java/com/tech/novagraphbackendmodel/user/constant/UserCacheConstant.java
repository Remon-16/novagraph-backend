package com.tech.novagraphbackendmodel.user.constant;

import com.tech.novagraphbackendcommon.utils.CacheUtils;

public interface UserCacheConstant {

    /**
     * 用户给剧本动态 hash key
     */
    public static final String USER_POST_THUMB_KEY_PREFIX = "pt:thumb:";

    /**
     * 临时 点赞记录 key
     */
    public static final String TEMP_THUMB_KEY_PREFIX = "pt:thumb:temp:";

    /**
     * 动态缓存
     */
    public static final String POST_CACHE_PREFIX = "ngpt:";

    public static final Long UN_THUMB_CONSTANT = 0L;

    /**
     * 获取 临时点赞记录 key
     */
    public static String getTempThumbKey(String time) {
        return TEMP_THUMB_KEY_PREFIX + time;
    }

    public static String getUserThumbKey(Long userId) {
        return USER_POST_THUMB_KEY_PREFIX + userId.toString();
    }

    public static String getPostCacheKey(String key){
        return POST_CACHE_PREFIX + key;
    }

    /**
     * 拼接 redis 的 Key 用于分布式 Redis 区分不同服务
     */
    public static String buildRedisKey(String key){
        return CacheUtils.APP_NAME + ":" + key;
    }
}
