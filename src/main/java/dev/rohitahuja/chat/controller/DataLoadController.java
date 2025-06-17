package dev.rohitahuja.chat.controller;

import dev.rohitahuja.chat.rag.DataLoader;
import org.springframework.ai.reader.TextReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataLoadController {

    private final DataLoader dataLoader;


    public DataLoadController(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @GetMapping("/data-load")
    public void dataLoad(@RequestParam String fileName) {

        dataLoader.load(fileName);
    }
}
