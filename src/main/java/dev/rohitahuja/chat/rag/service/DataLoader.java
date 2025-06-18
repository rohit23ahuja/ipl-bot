package dev.rohitahuja.chat.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader {
    private static final Logger _log = LoggerFactory.getLogger(DataLoader.class);
    private final JdbcClient jdbcClient;
    private final VectorStore vectorStore;

    private final String documentsPath;



    public DataLoader(JdbcClient jdbcClient, VectorStore vectorStore,
                      @Value("${documents.path}") String documentsPath) {
        this.jdbcClient = jdbcClient;
        this.vectorStore = vectorStore;
        this.documentsPath = documentsPath;
    }

    public void load(String fileName) {
        String filePath = documentsPath + "/" + fileName;
        _log.info("Reading document from {}", filePath);
        TextReader textReader = new TextReader(new FileSystemResource(filePath));
        textReader.getCustomMetadata().put("fileName", fileName);
        List<Document> documents = textReader.get();
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> splitDocuments = tokenTextSplitter.apply(documents);
        vectorStore.add(splitDocuments);
    }
}
