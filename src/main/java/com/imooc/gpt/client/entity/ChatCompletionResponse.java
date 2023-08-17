package com.imooc.gpt.client.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ChatCompletionResponse implements Serializable {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<ChatChoice> choices;
    private Usage usage;
}
