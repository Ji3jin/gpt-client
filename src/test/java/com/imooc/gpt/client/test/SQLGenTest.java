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

public class SQLGenTest {
    private ChatGPTClient chatGPTClient;

    @Before
    public void before() {
        chatGPTClient = LLMClientUtils.getChatGptClient();
    }

    @Test
    public void chat() {
        //定义两个函数 1. 给定表名查询元数据 2.根据sql查询数据
        List<ChatFunction> functions = getChatFunctions();
        //构造Prompt ， 根据输入  gpt判断是否需要调用函数， 根据用户输入，首先调用表元数据函数， 获取元数据之后，再调用sql查询
        Message sysMsg = Message.ofSystem("你是一个数据分析师，能够借助我提供的元数据信息，为我生成SQL，并且查询数据。");
        Message userMsg = Message.of("根据订单流水表(jd_sale_l)统计每年的商品销售数量？");
        //递归调用   根据gpt stop reason判断
        callChatGPT(Arrays.asList(sysMsg, userMsg), functions);

    }

    private List<ChatFunction> getChatFunctions() {
        List<ChatFunction> functions = new ArrayList<>();
        ChatFunction function = new ChatFunction();
        function.setName("getMetadata");
        function.setDescription("获取给定表的元数据信息");
        function.setParameters(ChatFunction.ChatParameter.builder()
                .type("object")
                .required(Arrays.asList("tableName"))
                .properties(JSON.parseObject("{\n" +
                        "          \"tableName\": {\n" +
                        "            \"type\": \"string\",\n" +
                        "            \"description\": \"The table name you want to select, e.g. t_goods_sale_d\"\n" +
                        "          }\n" +
                        "        }"))
                .build());
        functions.add(function);

        ChatFunction resultFunc = new ChatFunction();
        resultFunc.setName("selectData");
        resultFunc.setDescription("获取给定SQL的执行数据");
        resultFunc.setParameters(ChatFunction.ChatParameter.builder()
                .type("object")
                .required(Arrays.asList("sql"))
                .properties(JSON.parseObject("{\n" +
                        "          \"sql\": {\n" +
                        "            \"type\": \"string\",\n" +
                        "            \"description\": \"The sql you want to exec for select, e.g. select count(1) from test_table\"\n" +
                        "          }\n" +
                        "        }"))
                .build());
        functions.add(resultFunc);
        return functions;
    }

    private void callChatGPT(List<Message> messages, List<ChatFunction> functions) {
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
            if ("getMetadata".equals(functionCallName)) {
                String arguments = functionCall.getArguments();
                JSONObject jsonObject = JSON.parseObject(arguments);
                String tableName = jsonObject.getString("tableName");
                String metadata = getMetadata(tableName);
                res.setContent("");
                Message function1 = Message.ofFunction(metadata);
                function1.setName("getMetadata");
                ArrayList newList = new ArrayList<>(messages);
                newList.add(res);
                newList.add(function1);
                callChatGPT(newList, functions);
            }
            if ("selectData".equals(functionCallName)) {
                String arguments = functionCall.getArguments();
                JSONObject jsonObject = JSON.parseObject(arguments);
                String sql = jsonObject.getString("sql");
                String result = selectData(sql);
                res.setContent("");
                Message function1 = Message.ofFunction(result);
                function1.setName("selectData");
                ArrayList newList = new ArrayList<>(messages);
                newList.add(res);
                newList.add(function1);
                callChatGPT(newList, functions);
            }
        }
        System.out.println(res.getContent());
    }

    public String getMetadata(String tableName) {
        return "[\n" +
                "    {\n" +
                "        \"name\":\"uid\",\n" +
                "        \"type\":\"string\",\n" +
                "        \"comment\":\"订单id\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"name\",\n" +
                "        \"type\":\"string\",\n" +
                "        \"comment\":\"商品名称\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"price\",\n" +
                "        \"type\":\"double\",\n" +
                "        \"comment\":\"订单金额\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"name\":\"year\",\n" +
                "        \"type\":\"string\",\n" +
                "        \"comment\":\"订单记录产生的时间，年。如2021\"\n" +
                "    },\n" +
                "]";
    }

    public String selectData(String sql) {
        return "{\n" +
                "    \"colName\":[\"sales_count\",\"year\"],\n" +
                "    \"result\": [\n" +
                "        [1250,\"2021\"],[3250,\"2022\"],[8250,\"2021\"]\n" +
                "    ]\n" +
                "}";
    }
}
