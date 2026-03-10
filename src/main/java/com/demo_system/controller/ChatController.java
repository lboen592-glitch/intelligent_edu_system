package com.demo_system.controller;


import com.demo_system.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-chat")
public class ChatController {

    private final AiChatService aiChatService;

    // 流式接口
    @PostMapping(value = "/chat-stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> chatStream(@RequestBody Map<String, String> req, @RequestAttribute("userId") Long userId) {
        return aiChatService.chatStream(userId, req.get("prompt"));
    }
    @GetMapping("/search")
    public List<Map<String, String>> search(@RequestParam String q) {
        return aiChatService.searchOnline(q);
    }


}
