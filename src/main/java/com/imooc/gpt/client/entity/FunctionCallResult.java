package com.imooc.gpt.client.entity;


import lombok.Data;

@Data
public class FunctionCallResult {

    String name;

    String arguments;
}
