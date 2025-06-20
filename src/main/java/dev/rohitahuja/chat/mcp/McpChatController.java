package dev.rohitahuja.chat.mcp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class McpChatController {

    private final ChatClient chatClient;

    public McpChatController(ChatClient.Builder builder, ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = builder
                .defaultSystem("""
                        You are a helpful AI Assistant that answers based on knowledge available but also utilizes the tools that have been made available.
                        After receiving a tool's response, transform the raw data into a natural, conversational response. 
                        """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())
                .defaultToolCallbacks(toolCallbackProvider)
                .build();

    }

    @GetMapping("/chat/mcp")
    Flux<String> mcpChat(@RequestParam String message) {
        return chatClient
                .prompt()
                .user(message)
                .stream()
                .content();
    }
}

