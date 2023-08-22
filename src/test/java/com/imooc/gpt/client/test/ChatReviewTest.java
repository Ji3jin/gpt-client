package com.imooc.gpt.client.test;

import com.imooc.gpt.client.ChatGPTClient;
import com.imooc.gpt.client.entity.*;
import com.imooc.gpt.client.util.TokensUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatReviewTest {
    private ChatGPTClient chatGPTClient;
    @Before
    public void before() {
        chatGPTClient = LLMClientUtils.getChatGptClient();

    }
    @Test
    public void chat() {
        Message system = Message.ofSystem("你是一个机器学习领域的专家");
        Message message = Message.of("介绍机器学习常用算法类型，只需要告诉我名字，不需要额外的介绍");

        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(Model.GPT_3_5_TURBO.getName())
                .messages(Arrays.asList(system, message))
                .maxTokens(3000)
                .temperature(0.9)
                .build();
        ChatCompletionResponse response = chatGPTClient.chatCompletion(chatCompletion);
        System.out.println("-------------------chat-------------------");
        Message res = response.getChoices().get(0).getMessage();
        System.out.println(res.getContent());
    }


//    @Test
    public void chatReview() {
        Message system = Message.ofSystem("你是一个机器学习领域的专家");
        Message message = Message.of("介绍机器学习常用算法类型，只需要告诉我名字，不需要额外的介绍");
        Map<Integer, Integer> logitBiasMap = getBiasMap(Model.GPT_3_5_TURBO.getName(),Arrays.asList("回归","监督"));
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(Model.GPT_3_5_TURBO.getName())
                .messages(Arrays.asList(system, message))
                .maxTokens(3000)
                .n(3)
                .temperature(0.9)
                .frequencyPenalty(2)
                .presencePenalty(0)
                .logitBias(logitBiasMap)
                .build();
        ChatCompletionResponse response = chatGPTClient.chatCompletion(chatCompletion);
        System.out.println("-------------------chatReview-------------------");
        for(ChatChoice choice:response.getChoices()) {
            System.out.println(choice.getMessage().getContent());
        }
    }

    private Map<Integer, Integer> getBiasMap(String modelName,List<String> list) {
        Map<Integer, Integer> result = new HashMap<>();
        for(String word:list) {
            List<Integer> tokens = TokensUtil.getTokens(modelName, word);
            for(Integer t:tokens) {
                result.put(t,-100);
            }
        }
        return result;
    }
}
