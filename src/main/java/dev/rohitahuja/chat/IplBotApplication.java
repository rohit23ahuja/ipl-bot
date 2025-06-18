package dev.rohitahuja.chat;

import dev.rohitahuja.chat.toolcalling.WeatherConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(WeatherConfigProperties.class)
@SpringBootApplication
public class IplBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(IplBotApplication.class, args);
	}

}
