package com.imooc.gpt.client.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.imooc.gpt.client.util.TokensUtil;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletion implements Serializable {
    @NonNull
    @Builder.Default
    private String model = Model.GPT_3_5_TURBO_0613.getName();

    @NonNull
    private List<Message> messages;
    /**
     * 使用什么取样温度，0到2之间。越高越奔放。越低越保守。
     * <p>
     * 不要同时改这个和topP
     */
    @Builder.Default
    private double temperature = 0.9;

    /**
     * 0-1
     * 建议0.9
     * 不要同时改这个和temperature
     */
    @JsonProperty("top_p")
    @Builder.Default
    private double topP = 0.9;


    /**
     * auto
     */
    String function_call;

    List<ChatFunction> functions;

    /**
     * 结果数。
     */
    @Builder.Default
    private Integer n = 1;


    /**
     * 是否流式输出.
     * default:false
     */
    @Builder.Default
    private boolean stream = false;
    /**
     * 停用词
     */
    private List<String> stop;
    /**
     * 3.5 最大支持4096
     * 4.0 最大32k
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;


    @JsonProperty("presence_penalty")
    private double presencePenalty;

    /**
     * -2.0 ~~ 2.0
     */
    @JsonProperty("frequency_penalty")
    private double frequencyPenalty;

    @JsonProperty("logit_bias")
    private Map logitBias;
    /**
     * 用户唯一值，确保接口不被重复调用
     */
    private String user;

    public int countTokens() {
        return TokensUtil.tokens(this.model, this.messages);
    }
}
