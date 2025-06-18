package dev.rohitahuja.chat.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VectorSearchController {

    private final VectorStore vectorStore;

    public VectorSearchController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @GetMapping("/rag/similarity-search")
    public String similaritySearch(@RequestParam String text) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                .query(text)
                .topK(2)
                .build());
        List<String> contentList = results.stream().map(Document::getFormattedContent).toList();
        return String.join("\n", contentList);
    }

}
