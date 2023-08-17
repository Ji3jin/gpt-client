package com.imooc.gpt.client.test;

import com.imooc.gpt.client.ChatGPTClient;
import com.imooc.gpt.client.entity.ChatCompletion;
import com.imooc.gpt.client.entity.ChatCompletionResponse;
import com.imooc.gpt.client.entity.Message;
import com.imooc.gpt.client.entity.Model;
import com.imooc.gpt.client.util.Proxys;
import org.junit.Before;
import org.junit.Test;

import java.net.Proxy;
import java.util.Arrays;

public class ChatGPTClientTest {
    private ChatGPTClient chatGPTClient;
    @Before
    public void before() {
        Proxy proxy = Proxys.socks5("127.0.0.1", 7890);
        chatGPTClient = ChatGPTClient.builder()
                .apiKey("sk-6kchn0DjDasdsdfdqOJqkc3aIso5ct")
                .timeout(900)
                .proxy(proxy)
                .apiHost("https://api.openai.com/")
                .build()
                .init();

    }
    @Test
    public void chat() {
        Message system = Message.ofSystem("你是一个作家，学习过很多古诗");
        Message message = Message.of("写一首关于青春的七言绝句");

        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(Model.GPT_3_5_TURBO.getName())
                .messages(Arrays.asList(system, message))
                .maxTokens(3000)
                .temperature(0.9)
                .build();
        ChatCompletionResponse response = chatGPTClient.chatCompletion(chatCompletion);
        Message res = response.getChoices().get(0).getMessage();
        System.out.println(res.getContent());
    }

//    @Test
    public void tokens() {
        Message system = Message.ofSystem("你是一个作家，学习过很多古诗");
        Message message = Message.of("写一首关于青春的七言绝句");

        ChatCompletion chatCompletion1 = ChatCompletion.builder()
                .model(Model.GPT_3_5_TURBO.getName())
                .messages(Arrays.asList(system, message))
                .maxTokens(3000)
                .temperature(0.9)
                .build();
        ChatCompletion chatCompletion2 = ChatCompletion.builder()
                .model(Model.TEXT_DAVINCI_003.getName())
                .messages(Arrays.asList(system, message))
                .maxTokens(3000)
                .temperature(0.9)
                .build();

        System.out.println(chatCompletion1.countTokens());
        System.out.println(chatCompletion2.countTokens());
    }
}
