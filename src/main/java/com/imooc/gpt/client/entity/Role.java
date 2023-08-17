package com.imooc.gpt.client.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    SYSTEM("system"),
    USER("user"),
    FUNCTION("function"),
    ASSISTANT("assistant");

    private String value;
}
