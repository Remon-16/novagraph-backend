package com.tech.novagraphbackendcommon.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.util.DigestUtils;

public class CacheUtils {

    /**
     * 应用名称
     */
    public static final String APP_NAME = "novagraph";

    public static final String DESC = "descend";

    public static final String ASC = "ascend";

    public static String getHexLockString(Object queryCondition){
        String queryConditionString = JSONUtil.toJsonStr(queryCondition);
        return DigestUtils.md5DigestAsHex(queryConditionString.getBytes());
    }

    public static String getTimeSlice() {
        DateTime nowDate = DateUtil.date();
        // 获取到当前时间前最近的整数秒，比如当前 11:20:23 ，获取到 11:20:20
        return DateUtil.format(nowDate, "HH:mm:") + (DateUtil.second(nowDate) / 10) * 10;
    }

    public static String getCacheKey(String keyHead, String keyTail) {
        return keyHead + ":" + keyTail;
    }

    public static String getRedisCacheKey(String keyHead, String keyTail){
        return APP_NAME + ":" + keyHead + ":" + keyTail;
    }
}
