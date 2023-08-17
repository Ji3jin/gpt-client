package com.imooc.gpt.client.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.imooc.gpt.client.ChatGPTClient;
import com.imooc.gpt.client.entity.*;
import com.imooc.gpt.client.util.Proxys;
import org.junit.Before;
import org.junit.Test;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctionCallTest {
    private ChatGPTClient chatGPTClient;

    @Before
    public void before() {
        Proxy proxy = Proxys.http("127.0.0.1", 7890);

        chatGPTClient = ChatGPTClient.builder()
                .apiKey("sk-6kchn0DasdfqOJqkc3aIso5ct")
                .timeout(900)
                .proxy(proxy)
                .apiHost("https://api.openai.com/")
                .build()
                .init();

    }

    @Test
    public void chat() {
        List<ChatFunction> functions = new ArrayList<>();
        ChatFunction function = new ChatFunction();
        function.setName("getCurrentWeather");
        function.setDescription("获取给定位置的当前天气");
        function.setParameters(ChatFunction.ChatParameter.builder()
                .type("object")
                .required(Arrays.asList("location"))
                .properties(JSON.parseObject("{\n" +
                        "          \"location\": {\n" +
                        "            \"type\": \"string\",\n" +
                        "            \"description\": \"The city and state, e.g. San Francisco, " +
                        "CA\"\n" +
                        "          },\n" +
                        "          \"unit\": {\n" +
                        "            \"type\": \"string\",\n" +
                        "            \"enum\": [\"celsius\", \"fahrenheit\"]\n" +
                        "          }\n" +
                        "        }"))
                .build());
        functions.add(function);

        Message message = Message.of("上海的天气怎么样？");
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(Model.GPT_3_5_TURBO_16K.getName())
                .messages(Arrays.asList(message))
                .functions(functions)
                .maxTokens(8000)
                .temperature(0.9)
                .build();
        ChatCompletionResponse response = chatGPTClient.chatCompletion(chatCompletion);
        ChatChoice choice = response.getChoices().get(0);
        Message res = choice.getMessage();
        System.out.println(res);
        if ("function_call".equals(choice.getFinishReason())) {

            FunctionCallResult functionCall = res.getFunctionCall();
            String functionCallName = functionCall.getName();

            if ("getCurrentWeather".equals(functionCallName)) {
                String arguments = functionCall.getArguments();
                JSONObject jsonObject = JSON.parseObject(arguments);
                String location = jsonObject.getString("location");
                String unit = jsonObject.getString("unit");
                String weather = getCurrentWeather(location, unit);
                res.setContent("");
                callWithWeather(weather, res, functions);
            }
        }


    }

    private void callWithWeather(String weather, Message res, List<ChatFunction> functions) {
        Message message = Message.of("上海的天气怎么样？");
        Message function1 = Message.ofFunction(weather);
        function1.setName("getCurrentWeather");
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(Model.GPT_3_5_TURBO_16K.getName())
                .messages(Arrays.asList(message, res, function1))
                .functions(functions)
                .maxTokens(8000)
                .temperature(0.9)
                .build();
        ChatCompletionResponse response = chatGPTClient.chatCompletion(chatCompletion);
        ChatChoice choice = response.getChoices().get(0);
        Message res2 = choice.getMessage();
        //上海目前天气晴朗，气温为 22 摄氏度。
        System.out.println(res2.getContent());
    }

    public String getCurrentWeather(String location, String unit) {
        return "{ \"temperature\": 22, \"unit\": \"celsius\", \"description\": \"晴朗\" }";
    }
}

//本地有一个函数， 将函数信息告诉chatgpt。 并告诉chatgpt什么情况需要调用这个函数。  由chatgpt判断是否需要调用该函数，如果需要在交互中进行调用。类似于委托机制