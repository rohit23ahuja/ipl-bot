package dev.rohitahuja.chat.rag;

import dev.rohitahuja.chat.rag.service.DataLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataLoadController {

    private final DataLoader dataLoader;


    public DataLoadController(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @GetMapping("/chat/rag/data-load")
    public void dataLoad(@RequestParam String fileName) {
        dataLoader.load(fileName);
    }
}
