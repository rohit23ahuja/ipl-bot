package dev.rohitahuja.chat.toolcalling;

import dev.rohitahuja.chat.toolcalling.tools.DateTimeTools;
import dev.rohitahuja.chat.toolcalling.tools.EmailTools;
import dev.rohitahuja.chat.toolcalling.tools.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ToolCallingChatController {

    private final ChatClient chatClient;

    public ToolCallingChatController(ChatClient.Builder builder,
                                     WeatherConfigProperties weatherConfigProperties,
                                     EmailConfigProperties emailConfigProperties) {
        this.chatClient = builder
                .defaultSystem("You are a helpful AI Assistant that answers based on knowledge available but also utilizes the tools that have been made available.")
                .defaultTools(new WeatherTools(weatherConfigProperties), new EmailTools(emailConfigProperties), new DateTimeTools())
                .build();
    }

    @GetMapping("/chat/tool")
    public Flux<String> toolChat(@RequestParam String message) {
        return chatClient
                .prompt()
                .user(message)
                .stream()
                .content();
    }


//    private final ChatClient chatClient;
//
//    public ToolCallingController(ChatClient.Builder builder) {
//        this.chatClient = builder
//                .defaultSystem("You are a helpful AI Assistant answering questions about cities around the world.")
//                .defaultTools(WeatherTools.CURRENT_WEATHER_TOOL)
//                .build();
//    }
//
//    @GetMapping("/tool/chat")
//    public Flux<String> toolChat(@RequestParam String message){
//        return chatClient
//                .prompt()
//                .user(message)
//                .stream()
//                .content();
//    }


}
