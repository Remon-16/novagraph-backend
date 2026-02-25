package com.tech.novagraphbackendmodel.graph.valueobject;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 剧本审核状态枚举类
 */
@Getter
public enum ScreenplayReviewStatusEnum {

    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;

    private final int value;

    ScreenplayReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的 value
     * @return 枚举值
     */
    public static ScreenplayReviewStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ScreenplayReviewStatusEnum screenplayReviewStatusEnum : ScreenplayReviewStatusEnum.values()) {
            if (screenplayReviewStatusEnum.value == value) {
                return screenplayReviewStatusEnum;
            }
        }
        return null;
    }
}
