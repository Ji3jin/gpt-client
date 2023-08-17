package com.imooc.gpt.client.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Message implements Serializable {
    /**
     * 目前支持三种角色参考官网，进行情景输入：https://platform.openai.com/docs/guides/chat/introduction
     */
    private String role;
    private String content;
    private String name;

    @JsonProperty("function_call")
    private FunctionCallResult functionCall;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static Message of(String content) {

        return new Message(Role.USER.getValue(), content);
    }

    public static Message ofSystem(String content) {

        return new Message(Role.SYSTEM.getValue(), content);
    }

    public static Message ofAssistant(String content) {

        return new Message(Role.ASSISTANT.getValue(), content);
    }

    public static Message ofFunction(String function) {

        return new Message(Role.FUNCTION.getValue(), function);
    }
}
