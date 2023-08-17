package com.imooc.gpt.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ChatChoice implements Serializable {
    private long index;
    /**
     * 请求参数stream为true返回是delta
     */
    @JsonProperty("delta")
    private Message delta;
    @JsonProperty("message")
    private Message message;
    @JsonProperty("finish_reason")
    private String finishReason;
}
