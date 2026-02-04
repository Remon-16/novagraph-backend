package com.tech.novagraphbackendcommon.cache.valueobject;

import lombok.Getter;

@Getter
public enum UserActionEnum {
    // 点赞、关注、播放、收藏等
    INCR(1),
    // 取消点赞等
    DECR(-1),
    // 不发生改变
    NON(0),
    ;

    private final int value;

    UserActionEnum(int value) {
        this.value = value;
    }
}
