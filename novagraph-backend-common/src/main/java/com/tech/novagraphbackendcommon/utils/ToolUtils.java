package com.tech.novagraphbackendcommon.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ToolUtils {

    public static String getNowTimeString(){
        // 1. 获取当前时间（不含时区信息，最常用）
        LocalDateTime now = LocalDateTime.now();

        // 2. 定义格式化模式：年-月-日 时:分:秒
        // 常用占位符：yyyy(年) MM(月) dd(日) HH(24小时制小时) mm(分) ss(秒)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 3. 格式化时间对象为字符串
        return now.format(formatter);
    }
}
