package com.imooc.gpt.client.test;

import com.imooc.gpt.client.ChatGPTClient;
import com.imooc.gpt.client.ChatGPTStreamClient;
import com.imooc.gpt.client.util.Proxys;

import java.net.Proxy;

public class LLMClientUtils {

    private static String apiKey = System.getenv("OPENAI_APIKEY");
    private static Proxy proxy = Proxys.http("127.0.0.1", 7890);

    public static ChatGPTClient getChatGptClient() {
        return ChatGPTClient.builder()
                .apiKey(apiKey)
                .timeout(900)
                .proxy(proxy)
                .apiHost("https://api.openai.com/")
                .build()
                .init();
    }

    public static ChatGPTStreamClient getChatGptStreamClient() {
        return ChatGPTStreamClient.builder()
                .apiKey(apiKey)
                .proxy(proxy)
                .timeout(600)
                .apiHost("https://api.openai.com/")
                .build()
                .init();
    }

}
