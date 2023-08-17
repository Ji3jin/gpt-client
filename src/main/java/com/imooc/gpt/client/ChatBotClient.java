package com.imooc.gpt.client;

import com.imooc.gpt.client.entity.Message;
import com.imooc.gpt.client.listener.ConsoleStreamListener;
import com.imooc.gpt.client.util.ChatContextHolder;
import com.imooc.gpt.client.util.Proxys;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;


@Slf4j

public class ChatBotClient {

    public static Proxy proxy = Proxy.NO_PROXY;

    public static void main(String[] args) {

        System.out.println("ChatGPT - Java command-line interface");
        System.out.println("Press enter twice to submit your question.");
        System.out.println();
        System.out.println("按两次回车以提交您的问题！！！");
        String chatUuid = UUID.randomUUID().toString();
        String key = "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";
        proxy = Proxys.http("127.0.0.1", 7890);
        while (true) {
            String prompt = getInput("\nYou:\n");

            ChatGPTStreamClient chatGPT = ChatGPTStreamClient.builder()
                    .apiKey(key)
                    .proxy(proxy)
                    .build()
                    .init();
            System.out.println("AI: ");

            //卡住
            CountDownLatch countDownLatch = new CountDownLatch(1);

            Message message = Message.of(prompt);
            //todo
            ChatContextHolder.add(chatUuid, message);
            ConsoleStreamListener listener = new ConsoleStreamListener() {
                @Override
                public void onError(Throwable throwable, String response) {
                    throwable.printStackTrace();
                    countDownLatch.countDown();
                }
            };

            listener.setOnComplate(msg -> {
                //todo
                ChatContextHolder.add(chatUuid, Message.ofAssistant(msg));
                countDownLatch.countDown();
            });
            chatGPT.streamChatCompletion(ChatContextHolder.get(chatUuid), listener);

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    @SneakyThrows
    public static String getInput(String prompt) {
        System.out.print(prompt);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        String line;
        try {
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines.stream().collect(Collectors.joining("\n"));
    }

}

