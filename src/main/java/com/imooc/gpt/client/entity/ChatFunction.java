package com.imooc.gpt.client.entity;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatFunction {

    String name;
    String description;

    ChatParameter parameters;

    ChatCall call;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChatParameter {

        String type;
        List<String> required;
        Object properties;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public interface ChatCall {

        String run(String functionCallName,JSONObject params);
    }

}
