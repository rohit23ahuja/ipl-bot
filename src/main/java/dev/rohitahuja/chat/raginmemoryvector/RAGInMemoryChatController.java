package dev.rohitahuja.chat.raginmemoryvector;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class RAGInMemoryChatController {

    private final ChatClient chatClient;

    public RAGInMemoryChatController(ChatClient.Builder builder,
                                     @Qualifier("simpleVectorStore") VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(QuestionAnswerAdvisor
                        .builder(vectorStore)
                        .searchRequest(SearchRequest.builder().build())
                        .build())
                .build();
    }

    @GetMapping("/chat/rag-inmemory")
    public Flux<String> ragInMemoryChat(@RequestParam(value = "message",
                    defaultValue = "How many athletes are competing in Paris Olympics 2024?") String message){
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();

    }
}
