package com.example.ai_resume_analyzer.controller;

import com.example.ai_resume_analyzer.model.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    private  final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder){
        this.chatClient = builder.build();
    }

    @PostMapping("/ai")
    public String chat(@RequestBody ChatRequest request){
        return chatClient
                .prompt()
                .user(request.getQuestion())
                .call()
                .content();
    }
}
