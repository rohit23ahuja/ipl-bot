package dev.rohitahuja.chat.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class RagChatController {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    @Value("classpath:/prompts/ipl-bot-reference.st")
    private Resource sbPromptTemplate;

    public RagChatController(ChatClient.Builder builder,
                          VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())
                .build();
        this.vectorStore = vectorStore;
    }

    @GetMapping("/chat/rag")
    public Flux<String> chat(@RequestParam String message) {
        PromptTemplate promptTemplate = new PromptTemplate(sbPromptTemplate);
        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", message);
        promptParameters.put("documents", String.join("\n", findSimilarDocuments(message)));

        return chatClient.prompt(promptTemplate.create(promptParameters))
                .stream()
                .content();
    }

    private List<String> findSimilarDocuments(String message){
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.builder()
                .query(message)
                .topK(3).build());
        return similarDocuments.stream().map(Document::getFormattedContent).toList();

    }
}