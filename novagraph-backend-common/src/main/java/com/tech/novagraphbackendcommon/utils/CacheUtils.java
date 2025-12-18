package com.tech.novagraphbackendcommon.utils;

import cn.hutool.json.JSONUtil;
import org.springframework.util.DigestUtils;

public class CacheUtils {

    /**
     * 应用名称
     */
    public static final String APP_NAME = "novagraph";

    /**
     * 图片查询接口缓存名称 ngpic:query:queryCond(md5)
     */
    public static final String PICTURE_QUERY_CACHE = "ngpic:query";

    public static String getHexLockString(Object queryCondition){
        String queryConditionString = JSONUtil.toJsonStr(queryCondition);
        return DigestUtils.md5DigestAsHex(queryConditionString.getBytes());
    }

    public static String getPictureQueryCacheKey(Object queryCondition){
        String queryConditionString = JSONUtil.toJsonStr(queryCondition);
        String hashKey = DigestUtils.md5DigestAsHex(queryConditionString.getBytes());
        return PICTURE_QUERY_CACHE + ":" + hashKey;
    }
}
