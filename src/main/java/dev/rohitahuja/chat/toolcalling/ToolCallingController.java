package dev.rohitahuja.chat.toolcalling;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ToolCallingController {

    private final ChatModel chatModel;
    private final WeatherConfigProperties weatherConfigProperties;

    public ToolCallingController(ChatModel chatModel, WeatherConfigProperties weatherConfigProperties) {
        this.chatModel = chatModel;
        this.weatherConfigProperties = weatherConfigProperties;
    }

    @GetMapping("/tool/chat")
    public Flux<String>  toolChat(@RequestParam String message){
        return ChatClient.create(chatModel)
                .prompt()
                .user(message)
                .system("You are a helpful AI Assistant answering questions about cities around the world.")
                .tools(new WeatherTools(weatherConfigProperties))
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
