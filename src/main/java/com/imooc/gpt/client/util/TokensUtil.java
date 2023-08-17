package com.imooc.gpt.client.util;

import cn.hutool.core.util.StrUtil;
import com.imooc.gpt.client.entity.Message;
import com.imooc.gpt.client.entity.Model;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TokensUtil {

    private static final Map<String, Encoding> modelEncodingMap = new HashMap<>();
    private static final EncodingRegistry encodingRegistry = Encodings.newDefaultEncodingRegistry();

    static {
        for (Model model : Model.values()) {
            Optional<Encoding> encodingForModel = encodingRegistry.getEncodingForModel(model.getName());
            encodingForModel.ifPresent(encoding -> modelEncodingMap.put(model.getName(), encoding));
        }
    }

    /**
     * 计算tokens
     * @param modelName 模型名称
     * @param messages 消息列表
     * @return 计算出的tokens数量
     */

    public static int tokens(String modelName, List<Message> messages) {
        Encoding encoding = modelEncodingMap.get(modelName);
        if (encoding == null) {
            throw new IllegalArgumentException("Unsupported model: " + modelName);
        }

        int tokensPerMessage = 0;
        if (modelName.startsWith("gpt-4")) {
            tokensPerMessage = 3;
        } else if (modelName.startsWith("gpt-3.5-turbo")) {
            tokensPerMessage = 4; // every message follows <|start|>{role/name}\n{content}<|end|>\n
        }
        int sum = 0;
        for (Message message : messages) {
            sum += tokensPerMessage;
            sum += encoding.countTokens(message.getContent());
            sum += encoding.countTokens(message.getRole());
        }
        sum += 3;
        return sum;
    }

    public static List<Integer> getTokens(String modelName, String content) {
        Encoding encoding = modelEncodingMap.get(modelName);
        if (encoding == null) {
            throw new IllegalArgumentException("Unsupported model: " + modelName);
        }
        return encoding.encode(content);
    }
}
