package com.tech.novagraphbackendmodel.user.valueobject;

import lombok.Getter;

@Getter
public enum UserPostEnum {

    SCREENPLAY("剧本", "screenplay"),
    POST("动态", "post");

    private final String text;

    private final String value;

    UserPostEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
}
