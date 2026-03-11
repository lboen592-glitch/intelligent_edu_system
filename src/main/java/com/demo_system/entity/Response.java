package com.demo_system.entity;

import lombok.Data;


//统一响应返回类
@Data
public class Response {
    private String message;
    private Object data;
    private boolean success;

    public static Response ok(String message, Object data) {
        Response r = new Response();
        r.setSuccess(true);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    public static Response fail(String message) {
        Response r = new Response();
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }
}
