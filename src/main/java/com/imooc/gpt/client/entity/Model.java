package com.imooc.gpt.client.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Model {
    /**
     * gpt-3.5-turbo
     */
    GPT_3_5_TURBO("gpt-3.5-turbo"),
    GPT_3_5_TURBO_0613("gpt-3.5-turbo-0613"),
    GPT_3_5_TURBO_16K("gpt-3.5-turbo-16k"),
    TEXT_DAVINCI_003("text-davinci-003");
    private String name;


}
