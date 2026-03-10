package com.demo_system.entity;

import lombok.Getter;

import java.util.LinkedList;

@Getter
public class ChatSession {
    private final LinkedList<String> history = new LinkedList<>();
    //添加一轮对话
    public void addMessage(String role, String message){
        history.add(role + "：" + message);
        //限制12条
        if (history.size() > 12) history.removeFirst();
    }
    //拼接成prompt
    public String buildPrompt(){
        StringBuilder sb = new StringBuilder();
        for (String message : history){
            sb.append(message).append("\n");
        }
        return sb.toString();
    }
}
