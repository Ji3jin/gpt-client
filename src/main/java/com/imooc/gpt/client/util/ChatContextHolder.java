package com.imooc.gpt.client.util;

import com.imooc.gpt.client.entity.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatContextHolder {
    private static Map<String, List<Message>> context = new HashMap<>();

    public static List<Message> get(String id) {
        //todo  限制轮数，或者限制token数量

        List<Message> messages = context.get(id);
        if (messages == null) {
            messages = new ArrayList<>();
            context.put(id, messages);
        }

        return messages;
    }

    public static void add(String id, String msg) {

        Message message = Message.builder().content(msg).build();
        add(id, message);
    }

    public static void add(String id, Message message) {
        List<Message> messages = context.get(id);
        if (messages == null) {
            messages = new ArrayList<>();
            context.put(id, messages);
        }
        messages.add(message);
    }

    public static void remove(String id) {
        context.remove(id);
    }
}
