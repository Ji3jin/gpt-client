package com.imooc.gpt.client.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.imooc.gpt.client.ChatGPTClient;
import com.imooc.gpt.client.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class InteractiveDbSelectTest {

    private static Logger logger = LoggerFactory.getLogger(InteractiveDbSelectTest.class);

    private static ChatGPTClient chatGPTClient = LLMClientUtils.getChatGptClient();
    private static MysqlClient mysqlClient = MysqlClient.getInstance();
    private static List<ChatFunction> chatFunctions = getChatFunctions();

    /**
     * 启动一个命令行输入程序，根据输入返回特定结果
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        logger.info("欢迎来到会话程序！输入'quit'来结束会话。");
        // Call ChatGPT with the given message and chat functions
        callChatGPT(Arrays.asList(
                Message.ofSystem("You are primarily responsible for SQL recommendation and execution based on user input. However, you must follow the following rules:" +
                        "1. Recommended SQL must undergo metadata retrieval first." +
                        "2. Recommended SQL must include the database name." +
                        "3. Recommended SQL must be for existing tables." +
                        "Please wait patiently for specific user input. Then, follow the above rules for SQL recommendation and execution!")
        ), null);
        MysqlClient mysqlClient = MysqlClient.getInstance();
        try {
            mysqlClient.open();
            while (true) {
                System.out.print("你: ");
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("quit")) {
                    logger.info("会话结束。");
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
        functions.add(DBAdapter.getMetadataQueryFunction());
        functions.add(DBAdapter.getExecuteSQLFunction());
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
        Message res = choice.getMessage();
        if ("function_call".equals(choice.getFinishReason())) {
            FunctionCallResult functionCall = res.getFunctionCall();
            String functionCallName = functionCall.getName();
            for (ChatFunction function : functions) {
                if (function.getName().equalsIgnoreCase(functionCallName)) {
                    String arguments = functionCall.getArguments();
                    JSONObject jsonObject = JSON.parseObject(arguments);
                    if (null == function.getCall()) {
                        // 没有回调方法的function，是LLM针对上次内容的分析回复，不再调用LLM进行会话
                    } else {
                        logger.info(choice.toString());
                        // 具有回调方法的function，调用回调方法后再将结果输入给LLM进行分析
                        String callResult = function.getCall().run(functionCallName, jsonObject);
                        if (null != callResult) {
                            callChatGPT(AnalysisAdapter.getMessages(callResult), Arrays.asList(AnalysisAdapter.getFunction()));
                        }
                    }
                    break;
                }
            }
        } else {
            // 非回调方法，直接输出内容
            logger.info(res.getContent());
        }
    }


    static class AnalysisAdapter implements ChatFunction.ChatCall {

        @Override
        public String run(String functionCallName, JSONObject params) {
            if ("analysis".equalsIgnoreCase(functionCallName)) {
                logger.info(params.getString("analysis"));
            }
            return null;
        }

        /**
         * 获取到默认到function，一般用于提交信息给LLM，但是没有实际回调操作
         *
         * @return
         */
        private static ChatFunction getFunction() {
            return ChatFunction.builder().name("analysis").description("提交信息给LLM，用于保持上下文")
                    .parameters(ChatFunction.ChatParameter.builder().type("object")
                            .required(Arrays.asList("analysis")).properties(JSON.parseObject("{\n" +
                                    "          \"analysis\": {\n" +
                                    "            \"type\": \"string\",\n" +
                                    "            \"description\": \"按照列表形式输出我提交的内容，同时从专业角度给我一个分析后的介绍，请使用中文!\"\n" +
                                    "          }\n" +
                                    "        }")).build()).call(new AnalysisAdapter()).build();
        }

        private static List<Message> getMessages(String... messages) {
            ArrayList<Message> rs = new ArrayList<>();
            if (messages != null && messages.length > 0) {
                for (String message : messages) {
                    Message ms = Message.of(message);
                    ms.setName("analysis");
                    rs.add(ms);
                }
            } else {
                Message ms = Message.ofSystem("根据内容，进行分析后输出！");
                ms.setName("analysis");
                rs.add(ms);
            }
            return rs;
        }
    }


    static class DBAdapter implements ChatFunction.ChatCall {

        @Override
        public String run(String functionCallName, JSONObject params) {
            if (functionCallName.equalsIgnoreCase("executeSQL")) {
                return executeSQL(params);
            } else if (functionCallName.equalsIgnoreCase("getMetadata")) {
                return executeSQL(params);
            }
            return null;
        }

        public String executeSQL(JSONObject params) {
            try {
                return mysqlClient.executeQuery(params.getString("sql"));
            } catch (SQLException re) {
                logger.error("Error code: {} ", re.getErrorCode(), re);
                return "当前SQL执行错误，可能情况如下：" +
                        "1、没有选择库信息" +
                        "2、表名不正确" +
                        "你应该尝试先获取到元信息后，再进行SQL的推荐工作！";
            }
        }

        private static ChatFunction getExecuteSQLFunction() {
            return ChatFunction.builder().name("executeSQL").description("执行SQL语句")
                    .parameters(ChatFunction.ChatParameter.builder().type("object")
                            .required(Arrays.asList("sql")).properties(JSON.parseObject("{\n" +
                                    "          \"sql\": {\n" +
                                    "            \"type\": \"string\",\n" +
                                    "            \"description\": \"根据输入，给出适当SQL并返回，SQL中需要包含库信息；如果用户没有提示库信息，需要列出所有的库信息并要求用户输入库名称和表名称!\"\n" +
                                    "          }\n" +
                                    "        }")).build()).call(new DBAdapter()).build();
        }

        private static ChatFunction getMetadataQueryFunction() {
            return ChatFunction.builder().name("getMetadata").description("查询元信息")
                    .parameters(ChatFunction.ChatParameter.builder().type("object")
                            .required(Arrays.asList("sql")).properties(JSON.parseObject("{\n" +
                                    "          \"sql\": {\n" +
                                    "            \"type\": \"string\",\n" +
                                    "            \"description\": \"列出所有的库信息，同时列出所有库下的表信息\"\n" +
                                    "          }\n" +
                                    "        }")).build()).call(new DBAdapter()).build();
        }

    }
}
