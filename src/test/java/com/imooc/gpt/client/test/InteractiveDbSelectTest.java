package com.imooc.gpt.client.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.imooc.gpt.client.ChatGPTClient;
import com.imooc.gpt.client.entity.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class InteractiveDbSelectTest {

    private static ChatGPTClient chatGPTClient;
    private static MysqlClient mysqlClient = MysqlClient.getInstance();

    static {
        chatGPTClient = LLMClientUtils.getChatGptClient();
    }

    /**
     * 启动一个命令行输入程序，根据输入返回特定结果
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("欢迎来到会话程序！输入'quit'来结束会话。");

        MysqlClient mysqlClient = MysqlClient.getInstance();
        try {
            mysqlClient.open();

            List<ChatFunction> chatFunctions = getChatFunctions();

            while (true) {
                System.out.print("你: ");
                String userInput = scanner.nextLine();

                if (userInput.equalsIgnoreCase("quit")) {
                    System.out.println("会话结束。");
                    break;
                }

                callChatGPT(Arrays.asList(Message.of(userInput)), chatFunctions);
            }
        } catch (Exception e) {
            // 处理异常情况
            e.printStackTrace();
        } finally {
            try {
                mysqlClient.close();
            } catch (Exception e) {
                // 处理关闭连接异常情况
                e.printStackTrace();
            }
            scanner.close();
        }
    }


    /**
     * 组装DB查询端子，拆分为最小单元
     *
     * @return
     */
    private static List<ChatFunction> getChatFunctions() {
        List<ChatFunction> functions = new ArrayList<>();
        functions.add(getShowDbFunction());
        return functions;
    }


    private static void callChatGPT(List<Message> messages, List<ChatFunction> functions) {
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(Model.GPT_3_5_TURBO_16K.getName())
                .messages(messages)
                .functions(functions)
                .maxTokens(8000)
                .temperature(0.9)
                .build();
        ChatCompletionResponse response = chatGPTClient.chatCompletion(chatCompletion);
        ChatChoice choice = response.getChoices().get(0);
        System.out.println(choice);
        Message res = choice.getMessage();
        if ("function_call".equals(choice.getFinishReason())) {
            FunctionCallResult functionCall = res.getFunctionCall();
            String functionCallName = functionCall.getName();
            for (ChatFunction function : functions) {
                if (function.getName().equalsIgnoreCase(functionCallName)) {
                    String arguments = functionCall.getArguments();
                    JSONObject jsonObject = JSON.parseObject(arguments);
                    String callResult = function.getCall().run(functionCallName, jsonObject);
                    System.out.println(callResult);
                }
            }
        }
        System.out.println(res.getContent());
    }


    private static ChatFunction getShowDbFunction() {
        return ChatFunction.builder().name("showDb").description("列出所有的数据库")
                .parameters(ChatFunction.ChatParameter.builder().type("object")
                        .required(Arrays.asList("sql")).properties(JSON.parseObject("{\n" +
                                "          \"sql\": {\n" +
                                "            \"type\": \"string\",\n" +
                                "            \"description\": \"查询Mysql中所有的库信息SQL\"\n" +
                                "          }\n" +
                                "        }")).build()).call(new DBAdapter()).build();
    }


    static class DBAdapter implements ChatFunction.ChatCall {

        @Override
        public String run(String functionCallName, JSONObject params) {
            if (functionCallName.equalsIgnoreCase("showDb")) {
                return showDb(params);
            }
            return null;
        }

        public String showDb(JSONObject params) {
            return mysqlClient.executeQuery(params.getString("sql"));
        }

    }

}
