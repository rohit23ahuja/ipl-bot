package dev.rohitahuja.chat.multimodal;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MultiModalChatController {
    private final ChatClient chatClient;

    @Value("classpath:/images/diehard1.jpg")
    private Resource movieImage;

    @Value("classpath:/images/observability-setup.png")
    private Resource codeImage;


    public MultiModalChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/chat/multimodal")
    public String multimodalChat(@RequestParam String message){
        if(message.equalsIgnoreCase("describe movie image")){
            return chatClient.prompt()
                    .user(u -> u
                            .text("Can you please explain what you see in the following image?")
                            .media(MimeTypeUtils.IMAGE_JPEG, movieImage))
                    .call().content();
        } else if(message.equalsIgnoreCase("describe code from image")){
            return chatClient.prompt()
                    .user(u -> u
                            .text("The following is a screenshot of some code. Can you do your best to generate code that is in this screenshot?")
                            .media(MimeTypeUtils.IMAGE_PNG, codeImage))
                    .call().content();
        } else {
            return "not supported.";
        }

    }

}
