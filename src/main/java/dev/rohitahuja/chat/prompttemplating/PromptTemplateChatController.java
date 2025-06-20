package dev.rohitahuja.chat.prompttemplating;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class PromptTemplateChatController {

    private final ChatClient chatClient;
    @Value("classpath:/prompts/books.st")
    private Resource booksPromptResource;

    public PromptTemplateChatController(ChatClient.Builder builder){
        this.chatClient = builder
    .defaultSystem("you are an helpful AI assistant answering questions about books. do your best to provide correct answer. If you don't know the answer just say I don't know.")
    .build();
    }

    @GetMapping("/chat/prompt-templating")
    public Map<String, Object> promptTemplating(@RequestParam(value = "category", defaultValue = "technology") String category){
        MapOutputConverter mapOutputConverter = new MapOutputConverter();
        String format = mapOutputConverter.getFormat();
        PromptTemplate promptTemplate = new PromptTemplate(booksPromptResource);
        Prompt prompt = promptTemplate.create(Map.of("category", category, "format", format ));
        ChatClient.CallResponseSpec responseSpec = chatClient.prompt(prompt).call();
        return mapOutputConverter.convert(responseSpec.content());
    }
}