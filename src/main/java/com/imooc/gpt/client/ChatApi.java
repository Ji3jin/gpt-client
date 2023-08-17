package com.imooc.gpt.client;

import com.imooc.gpt.client.entity.ChatCompletion;
import com.imooc.gpt.client.entity.ChatCompletionResponse;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatApi {
    String CHAT_GPT_API_HOST = "https://api.openai.com/";

    @POST("v1/chat/completions")
    Single<ChatCompletionResponse> chatCompletion(@Body ChatCompletion chatCompletion);
}
