package com.imooc.gpt.client.test;

import com.imooc.gpt.client.ChatGPTStreamClient;
import com.imooc.gpt.client.entity.ChatCompletion;
import com.imooc.gpt.client.entity.Message;
import com.imooc.gpt.client.listener.ConsoleStreamListener;
import com.imooc.gpt.client.util.Proxys;
import org.junit.Before;
import org.junit.Test;

import java.net.Proxy;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class StreamClientTest {

    private ChatGPTStreamClient chatGPTStreamClient;

    @Before
    public void before() {
        chatGPTStreamClient = LLMClientUtils.getChatGptStreamClient();
    }
    @Test
    public void chatCompletions() {
        ConsoleStreamListener listener = new ConsoleStreamListener();
        Message message = Message.of("写一段七言绝句诗");
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(Arrays.asList(message))
                .build();
        chatGPTStreamClient.streamChatCompletion(chatCompletion, listener);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
