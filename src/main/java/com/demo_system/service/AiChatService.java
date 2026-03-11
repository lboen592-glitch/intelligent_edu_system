package com.demo_system.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface AiChatService {


    // 流式返回
    Flux<String> chatStream(Long userId, String userInput);

}
