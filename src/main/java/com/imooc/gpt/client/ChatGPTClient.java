package com.imooc.gpt.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import com.alibaba.fastjson.JSON;
import com.imooc.gpt.client.entity.BaseResponse;
import com.imooc.gpt.client.entity.ChatCompletion;
import com.imooc.gpt.client.entity.ChatCompletionResponse;
import com.imooc.gpt.client.entity.Message;
import io.reactivex.Single;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.net.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
@Setter
@Builder
public class ChatGPTClient {
    private String apiKey;

    private List<String> apiKeyList;

    @Builder.Default
    private String apiHost = ChatApi.CHAT_GPT_API_HOST;
    private ChatApi apiClient;
    private OkHttpClient okHttpClient;

    /**
     * 超时 默认300
     */
    @Builder.Default
    private long timeout = 300;
    /**
     * okhttp 代理
     */
    @Builder.Default
    private Proxy proxy = Proxy.NO_PROXY;


    public ChatGPTClient init() {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(chain -> {
            Request original = chain.request();
            String key = apiKey;
            if (apiKeyList != null && !apiKeyList.isEmpty()) {
                key = RandomUtil.randomEle(apiKeyList);
            }
            Request request = original.newBuilder()
                    .header(Header.AUTHORIZATION.getValue(), "Bearer " + key)
                    .header(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue())
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        }).addInterceptor(chain -> {
            Request original = chain.request();
            Response response = chain.proceed(original);
            if (!response.isSuccessful()) {
                String errorMsg = response.body().string();

                log.error("请求异常：{}", errorMsg);
                BaseResponse baseResponse = JSON.parseObject(errorMsg, BaseResponse.class);
                if (Objects.nonNull(baseResponse.getError())) {
                    log.error(baseResponse.getError().getMessage());
                    throw new RuntimeException(baseResponse.getError().getMessage());
                }
                throw new RuntimeException(errorMsg);
            }
            return response;
        });

        client.connectTimeout(timeout, TimeUnit.SECONDS);
        client.writeTimeout(timeout, TimeUnit.SECONDS);
        client.readTimeout(timeout, TimeUnit.SECONDS);
        if (Objects.nonNull(proxy)) {
            client.proxy(proxy);
        }
        OkHttpClient httpClient = client.build();
        this.okHttpClient = httpClient;
        this.apiClient = new Retrofit.Builder()
                .baseUrl(this.apiHost)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(ChatApi.class);

        return this;
    }

    public ChatCompletionResponse chatCompletion(ChatCompletion chatCompletion) {
        Single<ChatCompletionResponse> chatCompletionResponse =
                this.apiClient.chatCompletion(chatCompletion);
        return chatCompletionResponse.blockingGet();
    }

    public String chat(String message) {
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(Arrays.asList(Message.of(message)))
                .build();
        ChatCompletionResponse response = this.chatCompletion(chatCompletion);
        return response.getChoices().get(0).getMessage().getContent();
    }
}
