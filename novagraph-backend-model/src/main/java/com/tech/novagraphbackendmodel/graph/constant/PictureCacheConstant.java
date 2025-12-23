package com.tech.novagraphbackendmodel.graph.constant;

import cn.hutool.json.JSONUtil;
import org.springframework.util.DigestUtils;

public class PictureCacheConstant {
    /**
     * 图片查询接口缓存名称 ngpic:query:queryCond(md5)
     */
    public static final String PICTURE_QUERY_CACHE = "ngpic:query";

    public static final String PICTURE_CACHE = "ngpic";

    public static String getPictureQueryCacheKey(Object queryCondition){
        String queryConditionString = JSONUtil.toJsonStr(queryCondition);
        String hashKey = DigestUtils.md5DigestAsHex(queryConditionString.getBytes());
        return PICTURE_QUERY_CACHE + ":" + hashKey;
    }

    public static String getPictureCacheKey(String key){
        return PICTURE_CACHE + ":" + key;
    }
}
