package com.imooc.gpt.client.util;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class Proxys {
    public static Proxy http(String ip, int port) {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
    }

    public static Proxy socks5(String ip, int port) {
        return new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ip, port));
    }
}
