package com.tech.novagraphbackendmodel.user.constant;

import com.tech.novagraphbackendcommon.utils.CacheUtils;

public interface UserCacheConstant {
    /**
     * 用户信息相关
     */
    String USER_INFO_KEY_PREFIX = "user:";

    /*
     * 用户收藏相关
     */

    /**
     * 用户收藏夹
     */
    String USER_FD_KEY_PREFIX = "user:fd:";

    /**
     * 用户收藏临时
     */
    String TEMP_USER_FAVORITE_KEY_PREFIX = "user:favorite:temp:";

    /**
     * 用户收藏
     */
    String USER_FAVORITE_KEY_PREFIX = "user:favorite:";
    /**
     * 用户收藏总数
     */
    String USER_FAVORITE_TOTAL_KEY_PREFIX = "user:favorite:t:";

    /**
     * 用户收藏夹 Hash
     */
    String USER_FAVORITE_FOLDER_KEY_PREFIX = "user:favorite:folder:";

    /*
     * 用户动态点赞相关
     */

    /**
     * 用户给剧本动态 hash key
     */
    String USER_POST_THUMB_KEY_PREFIX = "pt:thumb:";

    /**
     * 临时 点赞记录 key
     */
    String TEMP_THUMB_KEY_PREFIX = "pt:thumb:temp:";

    /**
     * 动态缓存
     */
    String POST_CACHE_PREFIX = "ngpt:";

    Long UN_THUMB_CONSTANT = 0L;

    /*
     * 播放历史相关
     */
    /**
     * 用户播放历史 hash key
     */
    String USER_HIS_KEY_PREFIX = "usr:sp:his:";

    /**
     * 临时 播放历史 key
     */
    String TEMP_HIS_KEY_PREFIX = "usr:sp:his:temp:";

    /**
     * 剧本播放历史 Key
     */
    String SP_HIS_KEY_PREFIX = "sp:his:";

    /**
     * 剧本收藏 Key
     */
    String SP_FAVORITE_KEY_PREFIX = "sp:favorite:";

    /*
     * 用户关注相关
     *
     */

    /**
     * 用户关注临时键
     */
    String USER_FOLLOW_TEMP_KEY_PREFIX = "ng:usr:follow:temp:";
    /**
     * 用户关注
     */
    String USER_FOLLOWING_KEY_PREFIX = "ng:usr:following:";
    /**
     * 用户粉丝
     */
    String USER_FOLLOWER_KEY_PREFIX = "ng:usr:follower:";
    /**
     * 用户关注数量
     */
    String USER_FOLLOWING_COUNT_KEY_PREFIX = "ng:usr:following:count:";
    /**
     * 用户粉丝数量
     */
    String USER_FOLLOWER_COUNT_KEY_PREFIX = "ng:usr:follower:count:";

    Long UN_FOLLOWING_CONSTANT = 0L;

    /*
     * 静态方法
     */

    /*
     * 用户信息相关
     */
    static String getUserInfoKey(Long userId){
        return USER_INFO_KEY_PREFIX + userId;
    }

    /*
     * 用户收藏相关
     */
    static String getUserFdCache(String hex){
        return USER_FD_KEY_PREFIX + hex;
    }

    static String getUserFavoriteKey(Long userId){
        return USER_FAVORITE_KEY_PREFIX + userId;
    }

    static String getTempUserFavoriteKey(String tail){
        return TEMP_USER_FAVORITE_KEY_PREFIX + tail;
    }

    static String getUserFavoriteFolderKey(Long userId){
        return USER_FAVORITE_FOLDER_KEY_PREFIX + userId;
    }

    static String getUserFavoriteTotalKey(Long userId){
        return USER_FAVORITE_TOTAL_KEY_PREFIX + userId;
    }

    /*
     * 动态点赞相关
     */

    /**
     * 获取 临时点赞记录 key
     */
    static String getTempThumbKey(String time) {
        return TEMP_THUMB_KEY_PREFIX + time;
    }

    static String getUserThumbKey(Long userId) {
        return USER_POST_THUMB_KEY_PREFIX + userId.toString();
    }

    static String getPostCacheKey(String key){
        return POST_CACHE_PREFIX + key;
    }

    /*
     * 播放历史相关
     */
    static String getTempHisKey(String tail){
        return TEMP_HIS_KEY_PREFIX + tail;
    }

    static String getUserHisKey(Long userId){
        return USER_HIS_KEY_PREFIX + userId.toString();
    }

    static String getSpHisKey(String spId){
        return SP_HIS_KEY_PREFIX + spId;
    }

    static String getSpFavoriteKey(Long spId){
        return SP_FAVORITE_KEY_PREFIX + spId;
    }

    /*
     * 用户关注相关
     *
     */
    static String getUserFollowTempKey(String tail){
        return USER_FOLLOW_TEMP_KEY_PREFIX + tail;
    }

    static String getUserFollowingKey(String userId){
        return USER_FOLLOWING_KEY_PREFIX + userId;
    }

    static String getUserFollowerKey(String userId){
        return USER_FOLLOWER_KEY_PREFIX + userId;
    }

    static String getUserFollowingCountKey(String userId){
        return USER_FOLLOWING_COUNT_KEY_PREFIX + userId;
    }

    static String getUserFollowerCountKey(String userId){
        return USER_FOLLOWER_COUNT_KEY_PREFIX + userId;
    }


    /*
     * 公共方法
     */

    /**
     * 拼接 redis 的 Key 用于分布式 Redis 区分不同服务
     */
    static String buildRedisKey(String key){
        return CacheUtils.APP_NAME + ":" + key;
    }
}
