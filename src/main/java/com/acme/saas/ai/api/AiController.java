package com.acme.saas.ai.api;

import com.acme.saas.ai.service.RagChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    public record ChatRequest(@NotBlank String question) {}
    public record ChatResponse(String answer) {}

    private final RagChatService ragChatService;

    public AiController(RagChatService ragChatService) {
        this.ragChatService = ragChatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest req) {
        String answer = ragChatService.chat(req.question());
        return new ChatResponse(answer);
    }
}